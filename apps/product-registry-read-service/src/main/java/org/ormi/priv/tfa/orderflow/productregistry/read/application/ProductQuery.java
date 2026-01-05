package org.ormi.priv.tfa.orderflow.productregistry.read.application;

import org.ormi.priv.tfa.orderflow.kernel.product.ProductId;

/**
 * Interface scellée définissant les requêtes de lecture pour le registre de produits.
 *
 * <p>Utilise le pattern <b>sealed interface + records</b> (Java 17+) pour un
 * système de requêtes type-safe et exhaustif. Chaque implémentation représente
 * un type de requête spécifique avec ses paramètres validés à la compilation.</p>
 *
 * <h3>Patterns supportés</h3>
 * <ul>
 *   <li>{@link GetProductByIdQuery} : Recherche par ID unique</li>
 *   <li>{@link ListProductQuery} : Pagination simple</li>
 *   <li>{@link ListProductBySkuIdPatternQuery} : Filtrage par pattern SKU + pagination</li>
 * </ul>
 *
 * <h3>Exemples d'utilisation</h3>
 * <pre>
 * // Recherche par ID
 * ProductQuery byId = new GetProductByIdQuery(productId);
 *
 * // Liste paginée
 * ProductQuery list = new ListProductQuery(0, 20);
 *
 * // Recherche par pattern SKU
 * ProductQuery byPattern = new ListProductBySkuIdPatternQuery("ABC-*", 0, 10);
 * </pre>
 *
 * @version 1.0
 * @since 2026-01-05
 */
public sealed interface ProductQuery 
    permits GetProductByIdQuery, ListProductQuery, ListProductBySkuIdPatternQuery {

    /**
     * Requête de récupération d'un produit par son identifiant unique.
     *
     * <p>Utilisée pour les opérations de lecture pointue (get one).</p>
     *
     * @param productId l'identifiant unique du produit recherché
     */
    public record GetProductByIdQuery(ProductId productId) implements ProductQuery {
    }

    /**
     * Requête de liste paginée de tous les produits.
     *
     * <p>Supporte la pagination standard (zero-based index).</p>
     *
     * @param page le numéro de page (0 = première page)
     * @param size le nombre d'éléments par page (typiquement 10-100)
     */
    public record ListProductQuery(int page, int size) implements ProductQuery {
    }

    /**
     * Requête de liste filtrée par pattern SKU + pagination.
     *
     * <p>Permet de rechercher les produits dont l'identifiant SKU correspond
     * à un pattern (ex: "ABC-*", "PROD2024%"). Supporte les wildcards SQL-like.</p>
     *
     * @param skuIdPattern le pattern de filtrage SKU (support wildcards)
     * @param page le numéro de page (0 = première page)
     * @param size le nombre d'éléments par page
     */
    public record ListProductBySkuIdPatternQuery(
        String skuIdPattern, 
        int page, 
        int size
    ) implements ProductQuery {
    }
}
