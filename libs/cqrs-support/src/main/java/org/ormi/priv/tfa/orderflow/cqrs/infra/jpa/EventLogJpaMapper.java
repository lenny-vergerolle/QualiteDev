package org.ormi.priv.tfa.orderflow.cqrs.infra.jpa;

import org.mapstruct.Context;
import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import org.ormi.priv.tfa.orderflow.cqrs.DomainEvent;
import org.ormi.priv.tfa.orderflow.cqrs.DomainEvent.DomainEventPayload;
import org.ormi.priv.tfa.orderflow.cqrs.EventEnvelope;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Mapper MapStruct : EventEnvelope → EventLogEntity (Event Sourcing persistence).
 *
 * <p>Extrait les métadonnées de l'enveloppe + sérialise payload polymorphique en JSONB.
 * Utilisé par EventLogRepository pour append-only log.</p>
 *
 * <h3>Mappings expression custom</h3>
 * <table>
 *   <tr><th>Cible</th><th>Source</th><th>Expression</th></tr>
 *   <tr><td>aggregateType</td><td>envelope</td><td>envelope.aggregateType()</td></tr>
 *   <tr><td>payload</td><td>event.payload()</td><td>om.valueToTree()</td></tr>
 * </table>
 */
@Mapper(
    componentModel = "cdi",
    unmappedTargetPolicy = ReportingPolicy.IGNORE,
    injectionStrategy = InjectionStrategy.CONSTRUCTOR
)
public interface EventLogJpaMapper {

    /**
     * Mapping principal : runtime EventEnvelope → immutable EventLogEntity.
     *
     * <p>@Context ObjectMapper CDI pour JSONB payload.</p>
     */
    @Mapping(target = "aggregateType", expression = "java(envelope.aggregateType())")
    @Mapping(target = "aggregateId", expression = "java(envelope.aggregateId())")
    @Mapping(target = "aggregateVersion", expression = "java(envelope.sequence())")
    @Mapping(target = "eventType", expression = "java(resolveEventType(envelope.event()))")
    @Mapping(target = "eventVersion", expression = "java(resolveEventVersion(envelope.event()))")
    @Mapping(target = "occurredAt", expression = "java(envelope.timestamp())")
    @Mapping(target = "payload", expression = "java(toJson(envelope.event().payload(), objectMapper))")
    EventLogEntity toEntity(EventEnvelope<?> envelope, @Context ObjectMapper objectMapper);

    /**
     * Résout le type d'événement (ex: "ProductRegistered").
     */
    default String resolveEventType(DomainEvent event) {
        return event.eventType();
    }

    /**
     * Résout la version schéma événement (ex: 1 pour V1).
     */
    default int resolveEventVersion(DomainEvent event) {
        return event.version();
    }

    /**
     * Sérialise DomainEventPayload polymorphique → JSONB.
     *
     * <p>Supporte tous les payloads via Jackson polymorphism.</p>
     */
    default JsonNode toJson(DomainEventPayload payload, @Context ObjectMapper om) {
        return om.valueToTree(payload);
    }
}
