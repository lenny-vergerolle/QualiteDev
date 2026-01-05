package org.ormi.priv.tfa.orderflow.kernel.product;

import java.time.Instant;

import org.ormi.priv.tfa.orderflow.cqrs.EventEnvelope;
import org.ormi.priv.tfa.orderflow.kernel.product.ProductEventV1.ProductDescriptionUpdated;
import org.ormi.priv.tfa.orderflow.kernel.product.ProductEventV1.ProductNameUpdated;
import org.ormi.priv.tfa.orderflow.kernel.product.ProductEventV1.ProductRegistered;
import org.ormi.priv.tfa.orderflow.kernel.product.ProductEventV1.ProductRetired;

/**
 * Enveloppe type-safe pour événements produit V1 (hierarchy sealed-like).
 *
 * <p>Spécialise {@link EventEnvelope} pour ProductEventV1 :
 * <ul>
 *   <li>Type safety compile-time (switch exhaustive)</li>
 *   <li>Concrete classes par événement</li>
 *   <li>Utilisé par ProductViewProjector (pattern matching)</li>
 * </ul></p>
 *
 * <h3>Utilisation projecteur</h3>
 * <pre>
 * return switch (ev) {
 *   case ProductRegisteredEnvelope pre → handle(pre);
 *   case ProductRetiredEnvelope pre → handle(pre);
 *   // ...
 * };
 * </pre>
 */
public abstract class ProductEventV1Envelope<E extends ProductEventV1> 
        extends EventEnvelope<E> {

    protected ProductEventV1Envelope(E event, Long sequence, Instant timestamp) {
        super(event, sequence, timestamp);
    }

    /** Enregistrement produit */
    public static class ProductRegisteredEnvelope 
            extends ProductEventV1Envelope<ProductRegistered> {
        public ProductRegisteredEnvelope(ProductRegistered event, Long sequence, Instant timestamp) {
            super(event, sequence, timestamp);
        }
    }

    /** Retraite produit */
    public static class ProductRetiredEnvelope 
            extends ProductEventV1Envelope<ProductRetired> {
        public ProductRetiredEnvelope(ProductRetired event, Long sequence, Instant timestamp) {
            super(event, sequence, timestamp);
        }
    }

    /** Mise à jour nom */
    public static class ProductNameUpdatedEnvelope 
            extends ProductEventV1Envelope<ProductNameUpdated> {
        public ProductNameUpdatedEnvelope(ProductNameUpdated event, Long sequence, Instant timestamp) {
            super(event, sequence, timestamp);
        }
    }

    /** Mise à jour description */
    public static class ProductDescriptionUpdatedEnvelope 
            extends ProductEventV1Envelope<ProductDescriptionUpdated> {
        public ProductDescriptionUpdatedEnvelope(ProductDescriptionUpdated event, Long sequence, Instant timestamp) {
            super(event, sequence, timestamp);
        }
    }
}
