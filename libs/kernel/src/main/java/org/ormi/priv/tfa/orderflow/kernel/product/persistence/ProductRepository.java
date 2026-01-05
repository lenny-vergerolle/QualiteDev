package org.ormi.priv.tfa.orderflow.kernel.product.persistence;

import java.util.Optional;

import org.ormi.priv.tfa.orderflow.kernel.Product;
import org.ormi.priv.tfa.orderflow.kernel.product.ProductId;
import org.ormi.priv.tfa.orderflow.kernel.product.SkuId;

/**
 * Repository interface agrégat Produit (Write Model - Aggregate Store).
 *
 * <p>CRUD pour {@link Product} mutable (Command Side).
 * <em>Pas de queries complexes → read model dédié.</em></p>
 *
 * <h3>Opérations</h3>
 * <table>
 *   <tr><th>Méthode</th><th>Use Case</th></tr>
 *   <tr><td>save()</td><td>Apply events → mutate → persist</td></tr>
 *   <tr><td>findById()</td><td>Load aggregate → handle command</td></tr>
 *   <tr><td>existsBySkuId()</td><td>Duplicate SKU check</td></tr>
 * </table>
 *
 * <h3>CQRS séparation</h3>
 * <ul>
 *   <li>Write : ce repo (optimistic concurrency)</li>
 *   <li>Read : ProductViewRepository (denormalized)</li>
 * </ul>
 */
public interface ProductRepository {
    
    /**
     * Persiste agrégat (avec version optimistic concurrency).
     */
    void save(Product product);

    /**
     * Charge agrégat par ID (avec lock/version).
     */
    Optional<Product> findById(ProductId id);

    /**
     * Vérifie unicité SKU (pré-condition métier).
     */
    boolean existsBySkuId(SkuId skuId);
}
