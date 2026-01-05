package org.ormi.priv.tfa.orderflow.productregistry.read.infra.outbox;

import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

import org.jboss.logging.Logger;
import org.ormi.priv.tfa.orderflow.cqrs.Projector.ProjectionResult;
import org.ormi.priv.tfa.orderflow.cqrs.infra.jpa.OutboxEntity;
import org.ormi.priv.tfa.orderflow.cqrs.infra.persistence.OutboxRepository;
import org.ormi.priv.tfa.orderflow.kernel.common.AggregateType;
import org.ormi.priv.tfa.orderflow.kernel.product.jpa.ProductEventJpaMapper;
import org.ormi.priv.tfa.orderflow.kernel.product.persistence.ProductEventVersion;
import org.ormi.priv.tfa.orderflow.kernel.product.views.ProductView;
import org.ormi.priv.tfa.orderflow.productregistry.read.application.ProjectionDispatcher;

import io.quarkus.runtime.ShutdownEvent;
import io.quarkus.runtime.Startup;
import io.quarkus.runtime.StartupEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.control.ActivateRequestContext;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

/**
 * Poller partitionné asynchrone de l'Outbox Pattern (Transactional Outbox).
 *
 * <p>Traite les messages outbox de manière partitionnée et tolérante aux pannes
 * pour la projection CQRS des événements produit. Utilise N+1 threads :
 * 1 scheduler + N workers (1 par CPU core).</p>
 *
 * <h3>Configuration</h3>
 * <table>
 *   <tr><th>Paramètre</th><th>Valeur</th></tr>
 *   <tr><td>Partitions</td><td>CPU cores</td></tr>
 *   <tr><td>Batch size</td><td>10</td></tr>
 *   <tr><td>Poll interval</td><td>1s</td></tr>
 *   <tr><td>Max retries</td><td>3</td></tr>
 *   <tr><td>Retry delay</td><td>30s</td></tr>
 * </table>
 *
 * <h3>Flux de traitement</h3>
 * <ol>
 *   <li>Poll outbox (ready + ordered by version)</li>
 *   <li>Partition par aggregateId.hashCode() % PARTITIONS</li>
 *   <li>Process async → ProjectionDispatcher</li>
 *   <li>Suivi des échecs (blockedUntil map)</li>
 * </ol>
 */
@ApplicationScoped
@Startup
public class OutboxPartitionedPoller {

    /** Nombre de partitions = nombre de CPU cores */
    private static final int PARTITIONS = Runtime.getRuntime().availableProcessors();
    /** Taille batch par poll */
    private static final int BATCH_SIZE = 10;
    /** Intervalle de polling */
    private static final int POLL_INTERVAL_MS = 1000;
    /** Max tentatives avant abandon */
    private static final int MAX_RETRIES = 3;
    /** Délai retry après échec */
    private static final Duration RETRY_DELAY = Duration.ofSeconds(30);

    private static final Logger LOG = Logger.getLogger(OutboxPartitionedPoller.class);

    /** Scheduler polling fixe (1 thread) */
    private final ScheduledExecutorService pollScheduler = 
        Executors.newSingleThreadScheduledExecutor();
    
    /** Workers partitionnés (1 thread par partition) */
    private ExecutorService[] executors = IntStream.range(0, PARTITIONS)
            .mapToObj(i -> Executors.newSingleThreadExecutor(
                r -> new Thread(r, "outbox-poller-" + i)))
            .toArray(ExecutorService[]::new);

    /** Map thread-safe : aggregateId → blocked until */
    private final Map<UUID, Instant> blockedUntil = new ConcurrentHashMap<>();

    private final OutboxRepository outbox;
    private final ProjectionDispatcher dispatcher;
    private final ProductEventJpaMapper mapper;

    /**
     * Constructeur CDI.
     */
    @Inject
    public OutboxPartitionedPoller(
            OutboxRepository outboxRepository,
            ProjectionDispatcher dispatcher,
            ProductEventJpaMapper mapper) {
        this.outbox = outboxRepository;
        this.dispatcher = dispatcher;
        this.mapper = mapper;
    }

    /**
     * Démarre le polling à l'initialisation Quarkus (@Startup).
     */
    void onStart(@Observes StartupEvent event) {
        pollScheduler.scheduleWithFixedDelay(this::poll, 0, POLL_INTERVAL_MS, TimeUnit.MILLISECONDS);
        // TODO: Hey, log some info
        LOG.infof("OutboxPartitionedPoller STARTED: %d partitions, batch=%d, poll=%dms", 
            PARTITIONS, BATCH_SIZE, POLL_INTERVAL_MS);
    }

    /**
     * Arrête proprement les executeurs au shutdown.
     */
    void onStop(@Observes ShutdownEvent event) {
        pollScheduler.shutdownNow();
        Arrays.stream(executors).forEach(ExecutorService::shutdownNow);
        // TODO: Hey, log some info
        LOG.info("OutboxPartitionedPoller SHUTDOWN complete");
    }

    /**
     * Polling transactionnel + @ActivateRequestContext (CDI).
     *
     * <p>Charge les messages ready (non-bloqués, ordonnés par version).</p>
     */
    @ActivateRequestContext
    @Transactional
    protected void poll() {
        try {
            List<OutboxEntity> readyMessages = outbox
                    .fetchReadyByAggregateTypeOrderByAggregateVersion(
                        AggregateType.PRODUCT.value(), BATCH_SIZE, MAX_RETRIES);
            if (readyMessages.isEmpty()) return;
            
            readyMessages.forEach((msg) -> {
                UUID aggregateId = msg.getSourceEvent().getAggregateId();
                Instant blockedTime = blockedUntil.get(aggregateId);
                if (blockedTime != null && blockedTime.isAfter(Instant.now())) {
                    // Still blocked, skip processing
                    return;
                }
                // Partitionnement par hashcode
                int partition = Math.floorMod(aggregateId.hashCode(), PARTITIONS);
                executors[partition].submit(() -> process(msg));
            });
        } catch (Exception e) {
            LOG.error("Error occurred while polling outbox messages", e);
        }
    }

    /**
     * Traitement asynchrone d'un message outbox (dans worker partitionné).
     *
     * <ol>
     *   <li>V1 uniquement → ProjectionDispatcher</li>
     *   <li>Succès → delete</li>
     *   <li>NoOp/Failure → markFailed + block aggregateId</li>
     * </ol>
     */
    private void process(OutboxEntity outboxMsg) {
        var ev = outboxMsg.getSourceEvent();
        try {
            if (ev.getEventVersion() == ProductEventVersion.V1.getValue()) {
                final ProjectionResult<ProductView> result = dispatcher.dispatch(
                        mapper.toProductEventV1(ev));
                
                if (result.isSuccess()) {
                    outbox.delete(outboxMsg);
                    return;
                }
                if (result.isNoOp()) {
                    outbox.markFailed(outboxMsg, result.getNoopReason(),
                            Long.valueOf(RETRY_DELAY.toMillis()).intValue());
                }
                if (result.isFailure()) {
                    outbox.markFailed(outboxMsg, result.getError(), 
                            Long.valueOf(RETRY_DELAY.toMillis()).intValue());
                }
                // Block aggregate pour éviter spam
                blockedUntil.put(outboxMsg.getSourceEvent().getAggregateId(), 
                    Instant.now().plus(RETRY_DELAY));
            }
        } catch (Exception e) {
            LOG.errorf("ProjectionDispatcher FAILED: outbox=%d, aggregateId=%s: %s",
                    outboxMsg.getId(), ev.getAggregateId(), e.getMessage(), e);
            outbox.markFailed(outboxMsg, e.getMessage(), 
                    Long.valueOf(RETRY_DELAY.toMillis()).intValue());
            blockedUntil.put(outboxMsg.getSourceEvent().getAggregateId(), 
                Instant.now().plus(RETRY_DELAY));
        }
    }
}
