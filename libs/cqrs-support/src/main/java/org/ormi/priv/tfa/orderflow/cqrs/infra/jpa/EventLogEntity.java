package org.ormi.priv.tfa.orderflow.cqrs.infra.jpa;

import java.time.Instant;
import java.util.UUID;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import com.fasterxml.jackson.databind.JsonNode;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Entité JPA Event Sourcing / Event Log (immutable audit trail).
 *
 * <p>Journal transactionnel des événements de domaine (append-only).
 * Supporte Event Sourcing + CQRS projections + observabilité.</p>
 *
 * <h3>Table eventing.event_log</h3>
 * <ul>
 *   <li>Index composite : (aggregate_type, aggregate_id, aggregate_version)</li>
 *   <li>JSONB payload : événements sérialisés</li>
 *   <li>Partitionnable par aggregate_type/shardKey</li>
 * </ul>
 *
 * <h3>Index optimisé replay</h3>
 * <pre>SELECT * FROM event_log 
 * WHERE aggregate_type='Product' AND aggregate_id='uuid' 
 * ORDER BY aggregate_version;</pre>
 */
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PUBLIC)
@Entity
@Table(
    schema = "eventing",
    name = "event_log",
    indexes = {
        @Index(name = "ix_eventlog_aggregate", 
               columnList = "aggregate_type, aggregate_id, aggregate_version")
    })
public class EventLogEntity {
    
    /** ID auto-incrémenté (bigserial) */
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, updatable = false, columnDefinition = "bigserial")
    private Long id;
    
    /** Type agrégat (Product, Order...) */
    @Column(name = "aggregate_type", nullable = false, updatable = false, columnDefinition = "text")
    private String aggregateType;
    
    /** ID agrégat (UUID sharding) */
    @Column(name = "aggregate_id", nullable = false, updatable = false, columnDefinition = "uuid")
    private UUID aggregateId;
    
    /** Version agrégat (optimistic concurrency) */
    @Column(name = "aggregate_version", nullable = false, updatable = false, columnDefinition = "bigint")
    private Long aggregateVersion;
    
    /** Type événement (ProductRegistered...) */
    @Column(name = "event_type", nullable = false, updatable = false, columnDefinition = "text")
    private String eventType;
    
    /** Version schéma événement (V1, V2...) */
    @Column(name = "event_version", nullable = false, updatable = false, columnDefinition = "int")
    private int eventVersion;
    
    /** Timestamp UTC */
    @Column(name = "occurred_at", nullable = false, updatable = false, columnDefinition = "timestamptz")
    private Instant occurredAt;
    
    /** Payload JSONB (polymorphique) */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "payload", nullable = false, updatable = false, columnDefinition = "jsonb")
    private JsonNode payload;

    /**
     * TODO: implements metadata for observability
     * 
     * - correlation : global tracing of requests (ex: traceparent OpenTelemetry)
     * - causation : linking related events (parent event ID)
     * - tenant : multi-tenancy support (for security, quotas or data tagging purposes)
     * - shardKey : partitioning data for scalability (hash mod 16)
     * 
     * @Column(name = "correlation_id", columnDefinition = "uuid")
     * private UUID correlationId;
     * 
     * @Column(name = "causation_id", columnDefinition = "uuid") 
     * private UUID causationId;
     * 
     * @Column(name = "tenant_id", columnDefinition = "text")
     * private String tenantId;
     * 
     * @Column(name = "shard_key", columnDefinition = "int")
     * private Integer shardKey;
     */
}
