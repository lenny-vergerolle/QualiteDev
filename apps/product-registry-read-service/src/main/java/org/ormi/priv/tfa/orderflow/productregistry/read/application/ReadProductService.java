package org.ormi.priv.tfa.orderflow.productregistry.read.application;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.ormi.priv.tfa.orderflow.contracts.productregistry.v1.read.ProductStreamElementDto;
import org.ormi.priv.tfa.orderflow.kernel.product.ProductId;
import org.ormi.priv.tfa.orderflow.kernel.product.persistence.ProductViewRepository;
import org.ormi.priv.tfa.orderflow.kernel.product.views.ProductView;

import io.smallrye.mutiny.Multi;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

/**
 * Service de lecture pour le registre de produits (Read Model).
 *
 * <p>Fournit les opérations CRUD de lecture + streaming réactif via Mutiny.
 * Orchestre repository + broadcaster pour des réponses synchrones et asynchrones.</p>
 *
 * <h3>Capacités</h3>
 * <ul>
 *   <li>Recherche par ID (get one)</li>
 *   <li>Recherche paginée par pattern SKU</li>
 *   <li>Streaming d'événements par produit unique</li>
 *   <li>Streaming d'événements pour liste de produits</li>
 * </ul>
 */
@ApplicationScoped
public class ReadProductService {

    private final ProductViewRepository repository;
    private final ProductEventBroadcaster productEventBroadcaster;

    /**
     * Constructeur CDI.
     */
    @Inject
    public ReadProductService(
        ProductViewRepository repository,
        ProductEventBroadcaster productEventBroadcaster) {
        this.repository = repository;
        this.productEventBroadcaster = productEventBroadcaster;
    }

    /**
     * Recherche un produit par son identifiant unique.
     *
     * <p>Opération synchrone, retourne Optional pour gérer l'absence.</p>
     *
     * @param productId l'ID du produit
     * @return la vue du produit ou Optional.empty()
     */
    public Optional<ProductView> findById(ProductId productId) {
        return repository.findById(productId);
    }

    /**
     * Recherche paginée de produits par pattern SKU.
     *
     * <p>Supporte les wildcards (ex: "ABC-*", "2024%").</p>
     *
     * @param skuIdPattern pattern de filtrage SKU
     * @param page index de page (0-based)
     * @param size taille de page
     * @return résultat paginé avec total
     */
    public SearchPaginatedResult searchProducts(String skuIdPattern, int page, int size) {
        return new SearchPaginatedResult(
                repository.searchPaginatedViewsOrderBySkuId(skuIdPattern, page, size),
                repository.countPaginatedViewsBySkuIdPattern(skuIdPattern));
    }

    /**
     * Flux réactif des événements d'un produit spécifique.
     *
     * <p>Filtre le broadcaster global sur l'ID produit via Mutiny select/where.</p>
     *
     * @param productId ID du produit à streamer
     * @return Multi des événements du produit
     */
    public Multi<ProductStreamElementDto> streamProductEvents(ProductId productId) {
        return productEventBroadcaster.stream()
                .select().where(e -> e.productId().equals(productId.value().toString()));
    }

    /**
     * Flux réactif des événements pour une liste de produits (page).
     *
     * <p>Extrait les IDs de la page courante et filtre le broadcaster global.
     * Optimisé pour les tableaux de bord/listes paginées en temps réel.</p>
     *
     * @param skuIdPattern pattern SKU pour la page
     * @param page index de page
     * @param size taille de page
     * @return Multi des événements des produits de la page
     */
    public Multi<ProductStreamElementDto> streamProductListEvents(
            String skuIdPattern, int page, int size) {
        final List<ProductView> products = searchProducts(skuIdPattern, page, size).page();
        final List<UUID> productIds = products.stream()
                .map(p -> p.getId().value())
                .toList();
        return productEventBroadcaster.stream()
                .select().where(e -> productIds.contains(UUID.fromString(e.productId())));
    }

    /**
     * Résultat de recherche paginée.
     *
     * <p>Contient la page courante + total pour calculer les métadonnées
     * de pagination (pages totales, hasNext, etc.).</p>
     *
     * @param page liste des produits de la page courante
     * @param total nombre total de résultats matching
     */
    public record SearchPaginatedResult(List<ProductView> page, long total) {
    }
}
