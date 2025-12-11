package org.ormi.priv.tfa.orderflow.productregistry.infra.jpa;

import java.util.UUID;

import org.ormi.priv.tfa.orderflow.kernel.product.ProductLifecycle;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Entité JPA représentant un produit dans la base de données.
 * <p>
 * Correspond à la table <code>domain.products</code> avec un index unique sur le SKU
 * pour garantir l'unicité. Stocke toutes les informations essentielles d'un produit
 * ainsi que son cycle de vie et sa version pour la gestion de concurrence optimiste.
 */
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PUBLIC)
@Entity
@Table(
    schema = "domain",
    name = "products",
    indexes = {
        @Index(name = "ux_products_sku", columnList = "sku", unique = true)
    })
public class ProductEntity {
    
    /**
     * Identifiant unique UUID du produit (clé primaire).
     */
    @Id
    @Column(name = "id", nullable = false, updatable = false, columnDefinition = "uuid")
    private UUID id;
    
    /**
     * Nom du produit.
     */
    @Column(name = "name", nullable = false, columnDefinition = "text")
    private String name;
    
    /**
     * Description détaillée du produit.
     */
    @Column(name = "description", nullable = false, columnDefinition = "text")
    private String description;
    
    /**
     * Identifiant SKU unique du produit (9 caractères max, non modifiable).
     */
    @Column(name = "sku_id", nullable = false, updatable = false, length = 9, unique = true, columnDefinition = "varchar(9)")
    private String skuId;
    
    /**
     * État du cycle de vie du produit (ACTIVE, RETIRED, etc.).
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, columnDefinition = "text")
    private ProductLifecycle status;
    
    /**
     * Version du produit pour la gestion de concurrence optimiste.
     */
    @Column(name = "version", nullable = false, columnDefinition = "bigint")
    private Long version;
}
