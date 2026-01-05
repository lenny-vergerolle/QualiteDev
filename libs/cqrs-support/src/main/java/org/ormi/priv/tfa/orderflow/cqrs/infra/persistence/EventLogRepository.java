package org.ormi.priv.tfa.orderflow.cqrs.infra.persistence;

import org.ormi.priv.tfa.orderflow.cqrs.EventEnvelope;
import org.ormi.priv.tfa.orderflow.cqrs.infra.jpa.EventLogEntity;

/**
 * Repository interface pour Event Sourcing (append-only event log).
 *
 * <p>Contrat pour persister les {@link EventEnvelope} de manière :
 * <ul>
 *   <li>Transactionnelle (avec business logic)</li>
 *   <li>Immuable (append-only)</li>
 *   <li>Ordonnée (par aggregate version)</li>
 * </ul></p>
 *
 * <h3>Implémentations</h3>
 * <ul>
 *   <li>JpaEventLogRepository (Panache + PostgreSQL JSONB)</li>
 *   <li>KafkaEventLogRepository (futur?)</li>
 * </ul>
 */
public interface EventLogRepository {
    
    /**
     * Append événement au log (transactionnel).
     *
     * <p>Atomique : business tx + event log insert.
     * Génère EventLogEntity avec ID + métadonnées extraites.</p>
     *
     * @param eventLog enveloppe complète (event + sequence + timestamp)
     * @return entité persistée (avec ID généré)
     */
    EventLogEntity append(EventEnvelope<?> eventLog);
}
