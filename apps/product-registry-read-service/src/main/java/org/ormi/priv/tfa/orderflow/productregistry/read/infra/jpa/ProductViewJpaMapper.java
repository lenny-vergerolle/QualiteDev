package org.ormi.priv.tfa.orderflow.productregistry.read.infra.jpa;

import java.io.IOException;
import java.util.List;

import org.mapstruct.Context;
import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;
import org.ormi.priv.tfa.orderflow.kernel.product.ProductIdMapper;
import org.ormi.priv.tfa.orderflow.kernel.product.SkuIdMapper;
import org.ormi.priv.tfa.orderflow.kernel.product.views.ProductView;
import org.ormi.priv.tfa.orderflow.kernel.product.views.ProductView.ProductViewCatalogRef;
import org.ormi.priv.tfa.orderflow.kernel.product.views.ProductView.ProductViewEvent;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Mapper MapStruct entre ProductView (Domain) et ProductViewEntity (JPA).
 *
 * <p>Gère la sérialisation/désérialisation JSONB des champs complexes :
 * <code>events</code> → {@link ProductViewEvent}[] et
 * <code>catalogs</code> → {@link ProductViewCatalogRef}[]</p>
 *
 * <h3>Configuration MapStruct</h3>
 * <ul>
 *   <li>CDI : injection constructeur</li>
 *   <li>Ignore unmapped : tolérant</li>
 *   <li>@Context ObjectMapper : passé explicitement</li>
 * </ul>
 *
 * <h3>Mappings custom JSONB</h3>
 * <pre>
 * Domain List&lt;ProductViewEvent&gt;  ↔  JPA JsonNode (JSONB PostgreSQL)
 * Domain List&lt;ProductViewCatalogRef&gt; ↔ JPA JsonNode (JSONB PostgreSQL)
 * </pre>
 */
@Mapper(
    componentModel = "cdi",
    unmappedTargetPolicy = ReportingPolicy.IGNORE,
    injectionStrategy = InjectionStrategy.CONSTRUCTOR,
    uses = {ProductIdMapper.class, SkuIdMapper.class}
)
public interface ProductViewJpaMapper {

    /**
     * Crée une nouvelle entité JPA à partir de la vue domaine.
     *
     * <p>Sérialise <code>events</code> et <code>catalogs</code> en JSONB.</p>
     *
     * @param productView vue domaine source
     * @param objectMapper ObjectMapper CDI (@Context)
     * @return entité JPA complète
     */
    @Mapping(target = "events", expression = "java(productViewEventListToJsonNode(productView.getEvents(), objectMapper))")
    @Mapping(target = "catalogs", expression = "java(productViewCatalogRefListToJsonNode(productView.getCatalogs(), objectMapper))")
    ProductViewEntity toEntity(ProductView productView, @Context ObjectMapper objectMapper);

    /**
     * Reconstruit la vue domaine depuis l'entité JPA.
     *
     * <p>Désérialise les JSONB <code>events</code> et <code>catalogs</code>.</p>
     *
     * @param entity entité JPA source
     * @param objectMapper ObjectMapper CDI (@Context)
     * @return vue domaine complète
     */
    @Mapping(target = "events", expression = "java(jsonNodeToProductViewEventList(entity.getEvents(), objectMapper))")
    @Mapping(target = "catalogs", expression = "java(jsonNodeToProductViewCatalogRefList(entity.getCatalogs(), objectMapper))")
    ProductView toDomain(ProductViewEntity entity, @Context ObjectMapper objectMapper);

    /**
     * Met à jour une entité JPA existante depuis la vue domaine.
     *
     * <p>Merge partiel + resérialisation JSONB.</p>
     *
     * @param productView vue domaine source
     * @param entity entité JPA cible (@MappingTarget)
     * @param objectMapper ObjectMapper CDI (@Context)
     */
    @Mapping(target = "events", expression = "java(productViewEventListToJsonNode(productView.getEvents(), objectMapper))")
    @Mapping(target = "catalogs", expression = "java(productViewCatalogRefListToJsonNode(productView.getCatalogs(), objectMapper))")
    void updateEntity(ProductView productView, @MappingTarget ProductViewEntity entity, @Context ObjectMapper objectMapper);

    /**
     * Sérialise List&lt;ProductViewEvent&gt; → JsonNode (JSONB).
     */
    default JsonNode productViewEventListToJsonNode(List<ProductViewEvent> events, @Context ObjectMapper om) {
        return om.valueToTree(events);
    }

    /**
     * Sérialise List&lt;ProductViewCatalogRef&gt; → JsonNode (JSONB).
     */
    default JsonNode productViewCatalogRefListToJsonNode(List<ProductViewCatalogRef> catalogRefs, @Context ObjectMapper om) {
        return om.valueToTree(catalogRefs);
    }

    // === JSON deserialization helpers ===
    /**
     * Désérialise JsonNode → List&lt;ProductViewEvent&gt;.
     */
    default List<ProductViewEvent> jsonNodeToProductViewEventList(JsonNode node, @Context ObjectMapper om) {
        try {
            return om.readValue(
                om.treeAsTokens(node),
                new TypeReference<List<ProductViewEvent>>() {}
            );
        } catch (IOException e) {
            throw new IllegalStateException("Failed to deserialize ProductViewEvent list", e);
        }
    }

    /**
     * Désérialise JsonNode → List&lt;ProductViewCatalogRef&gt;.
     */
    default List<ProductViewCatalogRef> jsonNodeToProductViewCatalogRefList(JsonNode node, @Context ObjectMapper om) {
        try {
            return om.readValue(
                om.treeAsTokens(node),
                new TypeReference<List<ProductViewCatalogRef>>() {}
            );
        } catch (IOException e) {
            throw new IllegalStateException("Failed to deserialize ProductViewCatalogRef list", e);
        }
    }
}
