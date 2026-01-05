package org.ormi.priv.tfa.orderflow.cqrs;

import java.time.Instant;
import java.util.UUID;

/**
 * Enveloppe générique d'événement de domaine avec métadonnées.
 *
 * <p>Enrichit {@link DomainEvent} avec :
 * <ul>
 *   <li>sequence (optimistic concurrency)</li>
 *   <li>timestamp (audit trail)</li>
 *   <li>aggregateId/type (routage/sharding)</li>
 * </ul></p>
 *
 * <h3>Utilisation</h3>
 * <pre>
 * EventEnvelope&lt;ProductRegistered&gt; env = EventEnvelope.with(productRegistered, 1L);
 * projector.project(currentView, env);
 * </pre>
 *
 * <h3>Généricité</h3>
 * <p>EventEnvelope&lt;E extends DomainEvent&gt; → type-safe</p>
 */
public class EventEnvelope<E extends DomainEvent> {
    
    private final E event;
    private final Long sequence;
    private final Instant timestamp;

    /**
     * Constructeur principal (utiliser factory avec()).
     */
    public EventEnvelope(E event, Long sequence, Instant timestamp) {
        this.event = event;
        this.sequence = sequence;
        this.timestamp = timestamp;
    }

    /** ID agrégat (délégué) */
    public UUID aggregateId() {
        return event.aggregateId();
    }

    /** Type agrégat (routage) */
    public String aggregateType() {
        return event.aggregateType();
    }

    /** Événement payload */
    public E event() {
        return event;
    }

    /** Numéro de séquence (version) */
    public Long sequence() {
        return sequence;
    }

    /** Timestamp création */
    public Instant timestamp() {
        return timestamp;
    }

    /**
     * Factory : crée avec timestamp now().
     *
     * @param event événement domaine
     * @param sequence numéro séquence
     * @return enveloppe prête
     */
    public static <E extends DomainEvent> EventEnvelope<E> with(E event, Long sequence) {
        return new EventEnvelope<>(event, sequence, Instant.now());
    }
}
