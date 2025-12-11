package org.ormi.priv.tfa.orderflow.productregistry.application;

import org.ormi.priv.tfa.orderflow.cqrs.EventEnvelope;
import org.ormi.priv.tfa.orderflow.cqrs.infra.jpa.EventLogEntity;
import org.ormi.priv.tfa.orderflow.cqrs.infra.jpa.OutboxEntity;
import org.ormi.priv.tfa.orderflow.cqrs.infra.persistence.EventLogRepository;
import org.ormi.priv.tfa.orderflow.cqrs.infra.persistence.OutboxRepository;
import org.ormi.priv.tfa.orderflow.kernel.Product;
import org.ormi.priv.tfa.orderflow.kernel.product.ProductEventV1.ProductDescriptionUpdated;
import org.ormi.priv.tfa.orderflow.kernel.product.ProductEventV1.ProductNameUpdated;
import org.ormi.priv.tfa.orderflow.kernel.product.persistence.ProductRepository;
import org.ormi.priv.tfa.orderflow.productregistry.application.ProductCommand.UpdateProductDescriptionCommand;
import org.ormi.priv.tfa.orderflow.productregistry.application.ProductCommand.UpdateProductNameCommand;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

/**
 * Service métier pour la mise à jour des informations des produits existants.
 * <p>
 * Gère les commandes de modification du nom et de la description des produits,
 * publie les événements correspondants via le journal et l'outbox pattern.
 */
@ApplicationScoped
public class UpdateProductService {

    private final ProductRepository repository;
    private final EventLogRepository eventLog;
    private final OutboxRepository outbox;

    /**
     * Constructeur par injection de dépendances.
     *
     * @param repository dépôt pour la persistance des produits
     * @param eventLog dépôt pour le journal des événements
     * @param outbox dépôt pour la boîte de publication des événements
     */
    @Inject
    public UpdateProductService(
        ProductRepository repository,
        EventLogRepository eventLog,
        OutboxRepository outbox
    ) {
        this.repository = repository;
        this.eventLog = eventLog;
        this.outbox = outbox;
    }

    /**
     * Traite la commande de mise à jour du nom d'un produit.
     * <p>
     * Charge le produit, met à jour son nom via la méthode métier, persiste les
     * modifications et publie l'événement {@link ProductNameUpdated}.
     *
     * @param cmd commande contenant l'ID du produit et le nouveau nom
     * @throws IllegalArgumentException si le produit n'existe pas
     */
    @Transactional
    public void handle(UpdateProductNameCommand cmd) throws IllegalArgumentException {
        Product product = repository.findById(cmd.productId())
                .orElseThrow(() -> new IllegalArgumentException("Product not found"));
        EventEnvelope<ProductNameUpdated> event = product.updateName(cmd.newName());
        // Save domain object
        repository.save(product);
        // Append event to event log
        final EventLogEntity persistedEvent = eventLog.append(event);
        // Publish event to outbox
        outbox.publish(
            OutboxEntity.Builder()
                .sourceEvent(persistedEvent)
                .build()
        );
    }

    /**
     * Traite la commande de mise à jour de la description d'un produit.
     * <p>
     * Charge le produit, met à jour sa description via la méthode métier, persiste
     * les modifications et publie l'événement {@link ProductDescriptionUpdated}.
     *
     * @param cmd commande contenant l'ID du produit et la nouvelle description
     * @throws IllegalArgumentException si le produit n'existe pas
     */
    @Transactional
    public void handle(UpdateProductDescriptionCommand cmd) throws IllegalArgumentException {
        Product product = repository.findById(cmd.productId())
                .orElseThrow(() -> new IllegalArgumentException("Product not found"));
        EventEnvelope<ProductDescriptionUpdated> event = product.updateDescription(cmd.newDescription());
        // Save domain object
        repository.save(product);
        // Append event to event log
        final EventLogEntity persistedEvent = eventLog.append(event);
        // Publish event to outbox
        outbox.publish(
            OutboxEntity.Builder()
                .sourceEvent(persistedEvent)
                .build()
        );
    }
}
