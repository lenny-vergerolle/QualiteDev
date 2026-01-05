package org.ormi.priv.tfa.orderflow.cqrs.infra.jpa;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Entité JPA Transactional Outbox (Dual Write Pattern).
 *
 * <p>Table de file d'attente pour événements à propager (reliable delivery).
 * Liée 1:N à {@link EventLogEntity} (sourceEvent FK).</p>
 *
 * <h3>Table eventing.outbox</h3>
 * <ul>
 *   <li>Index next_attempt_at : polling efficace</li>
 *   <li>ManyToOne EAGER : dénormalisé (event payload)</li>
 * </ul>
 *
 * <h3>États</h3>
 * <table>
 *   <tr><th>Status</th><th>Action</th></tr>
 *   <tr><td>nextAttemptAt <= now() && attempts < max</td><td>process</td></tr>
 *   <tr><td>success</td><td>DELETE</td></tr>
 *   <tr><td>failed</td><td>UPDATE nextAttemptAt + attempts++</td></tr>
 * </table>
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Entity
@Table(schema = "eventing", 
       name = "outbox", 
       indexes = {
           @Index(name = "ix_outbox_ready", columnList = "next_attempt_at")
       })
public class OutboxEntity {
    
    /** ID auto-incrémenté */
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, updatable = false, columnDefinition = "bigserial")
    private Long id;
    
    /** Nombre tentatives livraison */
    @Column(name = "attempts", nullable = false, updatable = false, columnDefinition = "int")
    private int attempts;
    
    /** Prochaine tentative (indexé) */
    @Column(name = "next_attempt_at", nullable = false, updatable = false, columnDefinition = "timestamptz")
    private Instant nextAttemptAt;
    
    /** Dernière erreur (pour retry/debug) */
    @Column(name = "last_error", nullable = false, updatable = false, columnDefinition = "text")
    private String lastError;

    /** Événement source (FK → EventLogEntity, EAGER) */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "event_id", nullable = false, updatable = false, columnDefinition = "bigint")
    private EventLogEntity sourceEvent;

    /**
     * Builder fluide (Lombok-free).
     */
    public static OutboxEntityBuilder Builder() {
        return new OutboxEntityBuilder();
    }

    /**
     * Builder interne (immutable → mutable → immutable).
     */
    public static class OutboxEntityBuilder {
        private EventLogEntity sourceEvent;

        public OutboxEntityBuilder sourceEvent(EventLogEntity evt) {
            this.sourceEvent = evt;
            return this;
        }

        public OutboxEntity build() {
            OutboxEntity entity = new OutboxEntity();
            entity.sourceEvent = sourceEvent;
            return entity;
        }
    }
}
