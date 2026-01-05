package org.ormi.priv.tfa.orderflow.kernel.product.views;

import org.ormi.priv.tfa.orderflow.kernel.product.ProductId;
import org.ormi.priv.tfa.orderflow.kernel.product.ProductLifecycle;
import org.ormi.priv.tfa.orderflow.kernel.product.SkuId;

import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

/**
 * Vue légère produit pour listes/pagination (Read Model Summary).
 *
 * <p>Projection denormalized optimisée :
 * <ul>
 *   <li>Champs essentiels uniquement</li>
 *   <li>Compteur catalogs (vs full list)</li>
 *   <li>Immutable + validée</li>
 * </ul></p>
 *
 * <h3>Usage API REST</h3>
 * <pre>
 * PaginatedProductListDto : ProductSummaryDto[]
 * // vs ProductViewDto (full avec events[])
 * </pre>
 *
 * <h3>Diff ProductView</h3>
 * <table>
 *   <tr><th>Summary</th><th>View</th></tr>
 *   <tr><td>light (list)</td><td>full (detail)</td></tr>
 *   <tr><td>catalogs count</td><td>catalogs list</td></tr>
 * </table>
 */
@Getter
public class ProductSummary {
    
    @NotNull private final ProductId id;
    @NotNull private final SkuId skuId;
    @NotBlank private final String name;
    @NotNull private final ProductLifecycle status;
    @NotNull private final Integer catalogs;

    /**
     * Constructeur privé (Builder uniquement).
     */
    private ProductSummary(ProductId id, SkuId skuId, String name, 
                          ProductLifecycle status, Integer catalogs) {
        this.id = id;
        this.skuId = skuId;
        this.name = name;
        this.status = status;
        this.catalogs = catalogs;
    }

    /**
     * Builder fluide + validation.
     */
    public static ProductSummaryBuilder Builder() {
        return new ProductSummaryBuilder();
    }

    /**
     * Builder interne immutable.
     */
    public static final class ProductSummaryBuilder {
        private ProductId id;
        private SkuId skuId;
        private String name;
        private ProductLifecycle status;
        private Integer catalogs;

        public ProductSummaryBuilder id(ProductId id) { this.id = id; return this; }
        public ProductSummaryBuilder skuId(SkuId skuId) { this.skuId = skuId; return this; }
        public ProductSummaryBuilder name(String name) { this.name = name; return this; }
        public ProductSummaryBuilder status(ProductLifecycle status) { this.status = status; return this; }
        public ProductSummaryBuilder catalogs(Integer catalogs) { this.catalogs = catalogs; return this; }

        /**
         * Build + validation Bean Validation.
         */
        public ProductSummary build() {
            ProductSummary summary = new ProductSummary(id, skuId, name, status, catalogs);
            final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();
            final var violations = validator.validate(summary);
            if (!violations.isEmpty()) {
                throw new ConstraintViolationException(violations);
            }
            return summary;
        }
    }
}
