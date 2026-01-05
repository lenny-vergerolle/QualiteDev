package org.ormi.priv.tfa.orderflow.contracts.productregistry.v1.read;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * DTO complet de vue produit (Read Model) pour API REST v1.
 *
 * <p>Projection CQRS avec historique événements et références catalogues.
 * Polymorphisme Jackson sur payloads événements (@JsonTypeInfo).</p>
 *
 * <h3>Structure hiérarchique</h3>
 * <ul>
 *   <li>ProductViewDto (root)</li>
 *   <li>↳ ProductViewDtoEvent[] (historique)</li>
 *   <li>↳ ProductViewDtoCatalog[] (refs catalogues)</li>
 *   <li>↳ ProductViewEventDtoPayload (polymorphique)</li>
 * </ul>
 */
public record ProductViewDto(
        String id,
        String skuId,
        String name,
        String status,
        String description,
        List<ProductViewDtoCatalog> catalogs,
        List<ProductViewDtoEvent> events,
        String createdAt,
        String updatedAt) {

    /**
     * Référence catalogue (léger).
     */
    public static record ProductViewDtoCatalog(
            String id,
            String name) {
    }

    /**
     * Payloads polymorphiques des événements (Jackson sealed interface).
     *
     * <p>@JsonTypeInfo discrimine par "type" → concrete payload.</p>
     */
    @JsonTypeInfo(use = JsonTypeInfo.Id.NAME, 
                  include = JsonTypeInfo.As.PROPERTY, 
                  property = "type")
    @JsonSubTypes({
        @JsonSubTypes.Type(value = ProductViewEventDtoPayload.ProductRegisteredPayloadDto.class, name = "ProductRegistered"),
        @JsonSubTypes.Type(value = ProductViewEventDtoPayload.ProductNameUpdatedPayloadDto.class, name = "ProductNameUpdated"),
        @JsonSubTypes.Type(value = ProductViewEventDtoPayload.ProductDescriptionUpdatedPayloadDto.class, name = "ProductDescriptionUpdated"),
        @JsonSubTypes.Type(value = ProductViewEventDtoPayload.ProductRetiredPayloadDto.class, name = "ProductRetired")
    })
    public sealed interface ProductViewEventDtoPayload 
            permits ProductRegisteredPayloadDto, ProductNameUpdatedPayloadDto, 
                    ProductDescriptionUpdatedPayloadDto, ProductRetiredPayloadDto {
        
        /** Enregistrement produit */
        record ProductRegisteredPayloadDto(
                String skuId,
                String name,
                String description) implements ProductViewEventDtoPayload {
        }
        
        /** Mise à jour nom (old/new) */
        record ProductNameUpdatedPayloadDto(
                String oldName,
                String newName) implements ProductViewEventDtoPayload {
        }
        
        /** Mise à jour description (old/new) */
        record ProductDescriptionUpdatedPayloadDto(
                String oldDescription,
                String newDescription) implements ProductViewEventDtoPayload {
        }
        
        /** Retraite produit (empty payload) */
        record ProductRetiredPayloadDto() implements ProductViewEventDtoPayload {
        }
    }

    /**
     * Événement avec type + sequence + payload polymorphique.
     */
    public static record ProductViewDtoEvent(
            ProductViewDtoEventType type,
            String timestamp,
            Long sequence,
            ProductViewEventDtoPayload payload) {
    }

    /**
     * Types d'événements (bi-directionnel JSON ↔ enum).
     *
     * <p>@JsonValue/@JsonCreator pour sérialisation custom.</p>
     */
    public static enum ProductViewDtoEventType {
        REGISTERED("ProductRegistered"),
        NAME_UPDATED("ProductNameUpdated"),
        DESCRIPTION_UPDATED("ProductDescriptionUpdated"),
        RETIRED("ProductRetired");

        private final String value;

        ProductViewDtoEventType(String value) {
            this.value = value;
        }

        @JsonValue
        public String getValue() {
            return value;
        }

        @JsonCreator
        public static ProductViewDtoEventType fromValue(String value) {
            for (ProductViewDtoEventType type : values()) {
                if (type.value.equals(value)) {
                    return type;
                }
            }
            throw new IllegalArgumentException("Unknown " + ProductViewDtoEventType.class.getSimpleName() + ": " + value);
        }
    }
}
