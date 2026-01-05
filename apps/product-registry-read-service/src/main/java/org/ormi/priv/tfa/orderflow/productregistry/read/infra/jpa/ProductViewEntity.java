package org.ormi.priv.tfa.orderflow.productregistry.read.infra.jpa;

import java.time.Instant;
import java.util.UUID;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.ormi.priv.tfa.orderflow.kernel.product.ProductLifecycle;

import com.fasterxml.jackson.databind.JsonNode;

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
 * Entité JPA représentant la vue de lecture d'un produit (Read Model).
 *
 * <p>Stocke l'état projeté d'un produit avec historique des événements et métadonnées.
 * Utilisée par le module Product Registry Read (CQRS Read Side).</p>
 *
 * <h3>Structure de table</h3>
 * <ul>
 *   <li>Schema : <code>read_product_registry.product_view</code></li>
 *   <li>Index unique : <code>sku_id</code> (VARCHAR(9))</li>
 *   <li>JSONB : <code>events</code>, <code>catalogs</code> (PostgreSQL)</li>
 * </ul>
 *
 * <h3>Champs critiques</h3>
 * <table>
 *   <tr><th>Champ</th><th>Type</th><th>Contraintes</th></tr>
 *   <tr><td>id</td><td>UUID</td><td>PK, non-null</td></tr>
 *   <tr><td>sku_id</td><td>VARCHAR(9)</td><td>unique, non-null</td></tr>
 *   <tr><td>events</td><td>JSONB</td><td>historique projeté</td></tr>
 * </table>
 */
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PUBLIC)
@Entity
@Table(
    schema = "read_product_registry",
    name = "product_view",
    indexes = {
        @Index(name = "idx_prdview_sku", columnList = "sku_id")
    })
public class ProductViewEntity {
    
    /** Identifiant unique du produit (UUID) */
    @Id
    @Column(name = "id", nullable = false, updatable = false, columnDefinition = "uuid")
    private UUID id;
    
    /** Version d'optimistic concurrency */
    @Column(name = "_version", nullable = false, columnDefinition = "bigint")
    private Long version;
    
    /** SKU unique du produit (fixe 9 chars) */
    @Column(name = "sku_id", nullable = false, length = 9, unique = true, columnDefinition = "varchar(9)")
    private String skuId;
    
    /** Nom du produit */
    @Column(name = "name", nullable = false, columnDefinition = "text")
    private String name;
    
    /** Description détaillée */
    @Column(name = "description", nullable = false, columnDefinition = "text")
    private String description;
    
    /** État du cycle de vie */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, columnDefinition = "text")
    private ProductLifecycle status;
    
    /** Historique des événements projetés */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "events", nullable = false, columnDefinition = "jsonb")
    private JsonNode events;
    
    /** Liste des catalogues associés (JSONB array) */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "catalogs", nullable = false, columnDefinition = "jsonb")
    private JsonNode catalogs;
    
    /** Timestamp de création */
    @Column(name = "created_at", nullable = false, updatable = false, columnDefinition = "timestamptz")
    private Instant createdAt;
    
    /** Timestamp de dernière mise à jour */
    @Column(name = "updated_at", nullable = false, columnDefinition = "timestamptz")
    private Instant updatedAt;
}
