package org.ormi.priv.tfa.orderflow.productregistry.application;

import org.ormi.priv.tfa.orderflow.cqrs.EventEnvelope;
import org.ormi.priv.tfa.orderflow.cqrs.infra.jpa.EventLogEntity;
import org.ormi.priv.tfa.orderflow.cqrs.infra.jpa.OutboxEntity;
import org.ormi.priv.tfa.orderflow.cqrs.infra.persistence.EventLogRepository;
import org.ormi.priv.tfa.orderflow.cqrs.infra.persistence.OutboxRepository;
import org.ormi.priv.tfa.orderflow.kernel.Product;
import org.ormi.priv.tfa.orderflow.kernel.product.ProductEventV1.ProductRetired;
import org.ormi.priv.tfa.orderflow.kernel.product.persistence.ProductRepository;
import org.ormi.priv.tfa.orderflow.productregistry.application.ProductCommand.RetireProductCommand;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

/**
 * Service métier pour la mise à la retraite (retrait) de produits existants dans le registre.
 * <p>
 * Récupère le produit, déclenche son comportement de mise à la retraite, persiste les
 * modifications et publie l'événement {@link ProductRetired} via le journal et l'outbox.
 */
@ApplicationScoped
public class RetireProductService {

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
    public RetireProductService(
            ProductRepository repository,
            EventLogRepository eventLog,
            OutboxRepository outbox) {
        this.repository = repository;
        this.eventLog = eventLog;
        this.outbox = outbox;
    }

    /**
     * Traite la commande de mise à la retraite d'un produit existant.
     * <p>
     * Charge le produit depuis le dépôt, appelle sa méthode métier {@code retire()},
     * persiste les modifications et publie l'événement de mise à la retraite.
     *
     * @param cmd commande contenant l'identifiant du produit à retirer
     * @throws IllegalArgumentException si le produit n'existe pas
     */
    @Transactional
    public void retire(RetireProductCommand cmd) throws IllegalArgumentException {
        Product product = repository.findById(cmd.productId())
                .orElseThrow(() -> new IllegalArgumentException("Product not found"));
        EventEnvelope<ProductRetired> evt = product.retire();
        repository.save(product);
        // Append event to the log
        final EventLogEntity persistedEvent = eventLog.append(evt);
        // Publish outbox
        outbox.publish(OutboxEntity.Builder()
                .sourceEvent(persistedEvent)
                .build());
    }
}
