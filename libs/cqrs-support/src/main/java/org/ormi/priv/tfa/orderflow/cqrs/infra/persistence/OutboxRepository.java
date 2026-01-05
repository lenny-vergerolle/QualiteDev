package org.ormi.priv.tfa.orderflow.cqrs.infra.persistence;

import java.util.List;

import org.ormi.priv.tfa.orderflow.cqrs.infra.jpa.OutboxEntity;

/**
 * Repository interface Transactional Outbox Pattern (Reliable Delivery).
 *
 * <p>Contrat pour le polling + traitement idempotent des messages outbox :
 * <ul>
 *   <li>publish() : insert tx avec business logic</li>
 *   <li>fetchReady() : poll ordonné par aggregate version</li>
 *   <li>delete/markFailed() : lifecycle traitement</li>
 * </ul></p>
 *
 * <h3>Flux Outbox Poller</h3>
 * <pre>
 * 1. publish(event)           → INSERT (tx business)
 * 2. fetchReady(type, limit)  → SELECT ready ORDER BY version
 * 3. process() → success? delete() : markFailed()
 * </pre>
 */
public interface OutboxRepository {
    
    /**
     * Publish outbox (transactionnel avec business tx).
     */
    void publish(OutboxEntity entity);

    /**
     * Poll messages prêts (nextAttemptAt <= now() && attempts < maxRetries).
     *
     * <p>Ordonné par aggregate_version → respect causalité événements.</p>
     */
    List<OutboxEntity> fetchReadyByAggregateTypeOrderByAggregateVersion(
            String aggregateType, int limit, int maxRetries);

    /**
     * Delete après succès (idempotent).
     */
    void delete(OutboxEntity entity);

    /**
     * Mark failed (retry avec délai par défaut).
     */
    void markFailed(OutboxEntity entity, String err);

    /**
     * Mark failed avec délai custom (UPDATE attempts++ + nextAttemptAt).
     */
    void markFailed(OutboxEntity entity, String err, int retryAfter);
}
