package org.ormi.priv.tfa.orderflow.cqrs.infra.jpa;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;

import org.ormi.priv.tfa.orderflow.cqrs.infra.persistence.OutboxRepository;

import io.quarkus.arc.DefaultBean;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

/**
 * Repository JPA Panache pour Transactional Outbox Pattern.
 *
 * <p>Implémente le pattern Outbox pour "dual write" fiable :
 * <ul>
 *   <li>publish() : append transactionnel (avec business tx)</li>
 *   <li>fetchReady() : poll natif SQL optimisé</li>
 *   <li>delete/markFailed() : polling idempotent</li>
 * </ul></p>
 *
 * <h3>SQL natif externe</h3>
 * <p>Query dynamique depuis /db/queries/findReadyByAggregateTypeOrderByAggregateVersion.sql</p>
 */
@ApplicationScoped
@DefaultBean
public class JpaOutboxRepository 
        implements PanacheRepository<OutboxEntity>, OutboxRepository {
    
    /** Délai retry par défaut (5s) */
    private static final int DEFAULT_DELAY_MS = 5000;
    
    /** Query native optimisée (loadée depuis classpath) */
    private static final String SQL_FETCH_QUERY = 
        loadSQLQueryFromFile("/db/queries/findReadyByAggregateTypeOrderByAggregateVersion.sql");

    /**
     * Publish outbox (transactionnel avec business logic).
     *
     * <p>Atomique : business tx + outbox insert.</p>
     */
    @Override
    public void publish(OutboxEntity entity) {
        persist(entity);
    }

    /**
     * Poll messages ready (SQL natif + aggregate ordering).
     *
     * <p>Optimisé : nextAttemptAt <= now() && attempts < maxRetries<br>
     * ORDER BY aggregate_version → respect causalité.</p>
     */
    @Override
    @SuppressWarnings("unchecked")
    public List<OutboxEntity> fetchReadyByAggregateTypeOrderByAggregateVersion(
            String aggregateType, int limit, int maxRetries) {
        return (List<OutboxEntity>) getEntityManager()
                .createNativeQuery(SQL_FETCH_QUERY, OutboxEntity.class)
                .setParameter("aggregateTypes", aggregateType)
                .setParameter("maxAttempts", maxRetries)
                .setMaxResults(limit)
                .getResultList();
    }

    /**
     * Delete après traitement réussi (idempotent).
     */
    @Transactional
    @Override
    public void delete(OutboxEntity entity) {
        deleteById(entity.getId());
    }

    /**
     * Mark failed (retry avec délai par défaut).
     */
    @Override
    public void markFailed(OutboxEntity entity, String err) {
        markFailed(entity, err, DEFAULT_DELAY_MS);
    }

    /**
     * Mark failed avec délai custom (transactionnel).
     *
     * <p>UPDATE lastError + nextAttemptAt + attempts++.</p>
     */
    @Transactional
    @Override
    public void markFailed(OutboxEntity entity, String err, int delayMs) {
        update("lastError = ?1, nextAttemptAt = ?2, attempts = attempts + 1 WHERE id = ?3",
                err, Instant.now().plusMillis(delayMs), entity.getId());
    }

    /**
     * Charge SQL natif depuis classpath (queries externes).
     */
    private static String loadSQLQueryFromFile(String classpath) {
        try (InputStream is = JpaOutboxRepository.class.getResourceAsStream(classpath)) {
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
