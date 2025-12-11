package org.ormi.priv.tfa.orderflow.productregistry.infra.jpa;

import org.mapstruct.Builder;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;
import org.ormi.priv.tfa.orderflow.kernel.Product;
import org.ormi.priv.tfa.orderflow.kernel.product.ProductIdMapper;
import org.ormi.priv.tfa.orderflow.kernel.product.SkuIdMapper;

/**
 * Mapper MapStruct pour la conversion bidirectionnelle entre les objets domaine {@link Product}
 * et les entités JPA {@link ProductEntity}.
 * <p>
 * Généré avec CDI pour injection de dépendances, utilise les mappers d'identifiants et ignore
 * les champs non mappés. Supporte la création, la mise à jour et la conversion vers le domaine.
 */
@Mapper(
    componentModel = "cdi",
    builder = @Builder(disableBuilder = false),
    uses = { ProductIdMapper.class, SkuIdMapper.class },
    unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public abstract class ProductJpaMapper {

    /**
     * Convertit une entité JPA en objet domaine.
     *
     * @param entity entité JPA à convertir
     * @return objet domaine Product correspondant
     */
    public abstract Product toDomain(ProductEntity entity);

    /**
     * Met à jour une entité JPA existante avec les données d'un objet domaine.
     *
     * @param product objet domaine source
     * @param entity entité JPA cible à mettre à jour
     */
    public abstract void updateEntity(Product product, @MappingTarget ProductEntity entity);

    /**
     * Convertit un objet domaine en nouvelle entité JPA.
     *
     * @param product objet domaine à convertir
     * @return nouvelle entité JPA
     */
    public abstract ProductEntity toEntity(Product product);
}
