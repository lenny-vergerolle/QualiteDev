package org.ormi.priv.tfa.orderflow.productregistry.infra.jpa;

import java.util.Optional;
import java.util.UUID;

import org.ormi.priv.tfa.orderflow.kernel.Product;
import org.ormi.priv.tfa.orderflow.kernel.product.ProductId;
import org.ormi.priv.tfa.orderflow.kernel.product.ProductIdMapper;
import org.ormi.priv.tfa.orderflow.kernel.product.SkuId;
import org.ormi.priv.tfa.orderflow.kernel.product.SkuIdMapper;
import org.ormi.priv.tfa.orderflow.kernel.product.persistence.ProductRepository;

import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

/**
 * Implémentation JPA du dépôt des produits utilisant Panache et Hibernate ORM.
 * <p>
 * Gère la persistance des entités {@link Product} via des mappers pour convertir
 * entre le domaine et les entités JPA. Supporte la mise à jour, la recherche par ID
 * et la vérification d'unicité par SKU.
 */
@ApplicationScoped
public class JpaProductRepository implements PanacheRepositoryBase<ProductEntity, UUID>, ProductRepository {

    private final ProductJpaMapper mapper;
    private final ProductIdMapper productIdMapper;    
    private final SkuIdMapper skuIdMapper;

    /**
     * Constructeur par injection de dépendances.
     *
     * @param mapper mapper entre entités JPA et objets domaine
     * @param productIdMapper mapper pour les identifiants de produits
     * @param skuIdMapper mapper pour les identifiants de SKU
     */
    @Inject
    public JpaProductRepository(ProductJpaMapper mapper, ProductIdMapper productIdMapper, SkuIdMapper skuIdMapper) {
        this.mapper = mapper;
        this.productIdMapper = productIdMapper;
        this.skuIdMapper = skuIdMapper;
    }

    /**
     * Persiste ou met à jour un produit dans la base de données.
     * <p>
     * Si le produit existe déjà, met à jour l'entité existante. Sinon, crée une
     * nouvelle entité et la merge.
     *
     * @param product produit à persister
     */
    @Override
    @Transactional
    public void save(Product product) {
        findByIdOptional(productIdMapper.map(product.getId()))
                .ifPresentOrElse(e -> {
                    mapper.updateEntity(product, e);
                }, () -> {
                    ProductEntity newEntity = mapper.toEntity(product);
                    getEntityManager().merge(newEntity);
                });
    }

    /**
     * Recherche un produit par son identifiant.
     *
     * @param id identifiant du produit
     * @return produit trouvé ou Optional vide
     */
    @Override
    public Optional<Product> findById(ProductId id) {
        return findByIdOptional(productIdMapper.map(id))
                .map(mapper::toDomain);
    }

    /**
     * Vérifie si un produit existe déjà pour un SKU donné.
     *
     * @param skuId identifiant SKU à vérifier
     * @return true si un produit existe avec ce SKU
     */
    @Override
    public boolean existsBySkuId(SkuId skuId) {
        return count("skuId", skuIdMapper.map(skuId)) > 0;
    }
}
