package org.ormi.priv.tfa.orderflow.productregistry.application;

import org.ormi.priv.tfa.orderflow.cqrs.EventEnvelope;
import org.ormi.priv.tfa.orderflow.cqrs.infra.jpa.EventLogEntity;
import org.ormi.priv.tfa.orderflow.cqrs.infra.jpa.OutboxEntity;
import org.ormi.priv.tfa.orderflow.cqrs.infra.persistence.EventLogRepository;
import org.ormi.priv.tfa.orderflow.cqrs.infra.persistence.OutboxRepository;
import org.ormi.priv.tfa.orderflow.kernel.Product;
import org.ormi.priv.tfa.orderflow.kernel.product.ProductEventV1.ProductRegistered;
import org.ormi.priv.tfa.orderflow.kernel.product.persistence.ProductRepository;
import org.ormi.priv.tfa.orderflow.kernel.product.ProductId;
import org.ormi.priv.tfa.orderflow.productregistry.application.ProductCommand.RegisterProductCommand;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

/**
 * Service métier pour l'enregistrement de nouveaux produits dans le registre.
 * <p>
 * Gère la création d'un produit via une commande, vérifie l'unicité du SKU,
 * persiste l'entité domaine, publie l'événement {@link ProductRegistered} dans
 * le journal d'événements et le met dans la boîte de publication (outbox pattern).
 */
@ApplicationScoped
public class RegisterProductService {

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
    public RegisterProductService(
        ProductRepository repository,
        EventLogRepository eventLog,
        OutboxRepository outbox
    ) {
        this.repository = repository;
        this.eventLog = eventLog;
        this.outbox = outbox;
    }

    /**
     * Traite la commande d'enregistrement d'un nouveau produit.
     * <p>
     * Vérifie l'unicité du SKU, crée le produit, le persiste, publie l'événement
     * de création et le met dans l'outbox pour publication asynchrone.
     *
     * @param cmd commande contenant les informations du produit à créer
     * @return identifiant unique du produit créé
     * @throws IllegalArgumentException si un produit avec le même SKU existe déjà
     */
    @Transactional
    public ProductId handle(RegisterProductCommand cmd) throws IllegalArgumentException {
        if (repository.existsBySkuId(cmd.skuId())) {
            throw new IllegalArgumentException(String.format("SKU already exists: %s", cmd.skuId()));
        }
        Product product = Product.create(
                cmd.name(),
                cmd.description(),
                cmd.skuId());
        // Save domain object
        repository.save(product);
        EventEnvelope<ProductRegistered> evt = EventEnvelope.with(new ProductRegistered(product.getId(), product.getSkuId(), cmd.name(), cmd.description()), product.getVersion());
        // Appends event to the log
        final EventLogEntity persistedEvent = eventLog.append(evt);
        // Publish outbox
        outbox.publish(OutboxEntity.Builder()
                .sourceEvent(persistedEvent)
                .build());
        return product.getId();
    }
}
