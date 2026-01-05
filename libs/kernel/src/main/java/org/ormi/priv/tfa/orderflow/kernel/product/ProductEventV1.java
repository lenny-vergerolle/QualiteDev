package org.ormi.priv.tfa.orderflow.kernel.product;

import java.util.UUID;

import org.ormi.priv.tfa.orderflow.cqrs.DomainEvent;
import org.ormi.priv.tfa.orderflow.kernel.Product;

/**
 * Événements produit V1 (Event Sourcing - sealed interface hierarchy).
 *
 * <p>Implémente {@link DomainEvent} avec :
 * <ul>
 *   <li>Versionning (EVENT_VERSION = 1)</li>
 *   <li>Agrégat fixe : Product</li>
 *   <li>Payloads polymorphiques (sealed)</li>
 * </ul></p>
 *
 * <h3>Événements supportés</h3>
 * <table>
 *   <tr><th>Événement</th><th>Payload</th></tr>
 *   <tr><td>ProductRegistered</td><td>skuId, name, description</td></tr>
 *   <tr><td>ProductRetired</td><td>Empty</td></tr>
 *   <tr><td>ProductNameUpdated</td><td>oldName, newName</td></tr>
 *   <tr><td>ProductDescriptionUpdated</td><td>oldDesc, newDesc</td></tr>
 * </table>
 */
public sealed interface ProductEventV1 extends DomainEvent {
    
    /** Version schéma V1 */
    public static final int EVENT_VERSION = 1;

    /**
     * Métadonnées agrégat (Product).
     */
    default String aggregateType() {
        return Product.class.getSimpleName();
    }

    /** ID produit (sharding/routage) */
    ProductId productId();

    /** UUID agrégat (délégué) */
    default UUID aggregateId() {
        return productId().value();
    }

    /** Version fixe V1 */
    @Override
    default public int version() {
        return EVENT_VERSION;
    }

    /** Payloads sealed (polymorphisme) */
    public sealed interface ProductEventV1Payload extends DomainEventPayload {}

    /** Payload vide (ProductRetired) */
    public static final class Empty implements ProductEventV1Payload {}

    /**
     * Enregistrement produit (init state).
     */
    public final class ProductRegistered implements ProductEventV1 {
        private final ProductId productId;
        private final ProductRegisteredPayload payload;

        public ProductRegistered(ProductId productId, SkuId skuId, String name, String description) {
            this.productId = productId;
            this.payload = new ProductRegisteredPayload(skuId.value(), name, description);
        }

        @Override public ProductId productId() { return productId; }
        @Override public ProductRegisteredPayload payload() { return payload; }

        public static record ProductRegisteredPayload(
                String skuId, String name, String description) implements ProductEventV1Payload {
        }
    }

    /**
     * Retraite produit (soft delete).
     */
    public final class ProductRetired implements ProductEventV1 {
        private final ProductId productId;

        public ProductRetired(ProductId productId) {
            this.productId = productId;
        }

        @Override public ProductId productId() { return productId; }
        @Override public ProductEventV1Payload payload() { return new ProductEventV1Payload.Empty(); }
    }

    /**
     * Mise à jour nom (audit old/new).
     */
    public final class ProductNameUpdated implements ProductEventV1 {
        private final ProductId productId;
        private final ProductNameUpdatedPayload payload;

        public ProductNameUpdated(ProductId productId, String oldName, String newName) {
            this.productId = productId;
            this.payload = new ProductNameUpdatedPayload(oldName, newName);
        }

        @Override public ProductId productId() { return productId; }
        @Override public ProductNameUpdatedPayload payload() { return payload; }

        public static record ProductNameUpdatedPayload(
                String oldName, String newName) implements ProductEventV1Payload {
        }
    }

    /**
     * Mise à jour description (audit old/new).
     */
    public final class ProductDescriptionUpdated implements ProductEventV1 {
        private final ProductId productId;
        private final ProductDescriptionUpdatedPayload payload;

        public ProductDescriptionUpdated(ProductId productId, String oldDescription, String newDescription) {
            this.productId = productId;
            this.payload = new ProductDescriptionUpdatedPayload(oldDescription, newDescription);
        }

        @Override public ProductId productId() { return productId; }
        @Override public ProductDescriptionUpdatedPayload payload() { return payload; }

        public static record ProductDescriptionUpdatedPayload(
                String oldDescription, String newDescription) implements ProductEventV1Payload {
        }
    }
}
