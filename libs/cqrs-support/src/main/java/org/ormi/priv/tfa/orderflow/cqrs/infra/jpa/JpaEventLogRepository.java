package org.ormi.priv.tfa.orderflow.cqrs.infra.jpa;

import org.ormi.priv.tfa.orderflow.cqrs.EventEnvelope;
import org.ormi.priv.tfa.orderflow.cqrs.infra.persistence.EventLogRepository;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.quarkus.arc.DefaultBean;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

/**
 * Repository JPA Panache pour EventLogEntity (Event Sourcing append-only).
 *
 * <p>Implémente {@link EventLogRepository} avec :
 * <ul>
 *   <li>PanacheRepository (CRUD + queries)</li>
 *   <li>@Transactional append() atomique</li>
 *   <li>EventLogJpaMapper (EventEnvelope → Entity)</li>
 * </ul></p>
 *
 * <h3>Flux append</h3>
 * <pre>
 * EventEnvelope&lt;ProductRegistered&gt; env = ...;
 * EventLogEntity log = repository.append(env);  // persist + flush
 * </pre>
 */
@ApplicationScoped
@DefaultBean
public class JpaEventLogRepository 
        implements PanacheRepository<EventLogEntity>, EventLogRepository {

    private final EventLogJpaMapper mapper;
    private final ObjectMapper objectMapper;

    /**
     * Constructeur CDI (@Inject mappers).
     */
    @Inject
    public JpaEventLogRepository(EventLogJpaMapper mapper, ObjectMapper objectMapper) {
        this.mapper = mapper;
        this.objectMapper = objectMapper;
    }

    /**
     * Append événement (transactionnel + immutable).
     *
     * <p>1. Map → EventLogEntity<br>
     * 2. Panache persist()<br>
     * 3. Auto-flush (@Transactional)</p>
     *
     * @param eventLog enveloppe avec métadonnées
     * @return entité persistée (avec ID généré)
     */
    @Override
    @Transactional
    public EventLogEntity append(EventEnvelope<?> eventLog) {
        EventLogEntity entity = mapper.toEntity(eventLog, objectMapper);
        persist(entity);
        return entity;
    }
}
