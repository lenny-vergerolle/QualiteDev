package org.ormi.priv.tfa.orderflow.kernel.product;

import java.util.UUID;

import jakarta.validation.constraints.NotNull;

/**
 * Value Object immutable : Identifiant Produit (UUID).
 *
 * <p>Wrapper type-safe autour UUID avec :
 * <ul>
 *   <li>Validation @NotNull</li>
 *   <li>Factory newId() → random UUID</li>
 *   <li>Utilisé partout : Product, ProductEventV1, ProductView</li>
 * </ul></p>
 *
 * <h3>DDD Benefits</h3>
 * <ul>
 *   <li>Type safety vs raw UUID</li>
 *   <li>Immutabilité garantie</li>
 *   <li>Bean Validation</li>
 * </ul>
 *
 * <h3>Usage</h3>
 * <pre>
 * ProductId id = ProductId.newId();
 * Product product = Product.create("name", "desc", skuId);
 * EventEnvelope.with(event, 1L); // aggregateId() → id.value()
 * </pre>
 */
public record ProductId(@NotNull UUID value) {
    
    /**
     * Factory : UUID aléatoire v4.
     */
    public static ProductId newId() {
        return new ProductId(UUID.randomUUID());
    }
}
