package org.ormi.priv.tfa.orderflow.store.infra.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeName;

/**
 * DTO pour mise à jour batch de produit (multi-opérations polymorphiques).
 *
 * <p>Supporte UPDATE_NAME + UPDATE_DESCRIPTION en un seul appel via
 * polymorphisme Jackson (@JsonTypeInfo + @JsonSubTypes).</p>
 *
 * <h3>JSON Example</h3>
 * <pre>
 * {
 *   "id": "550e8400-e29b-41d4-a716-446655440000",
 *   "operations": [
 *     {
 *       "type": "UpdateProductName",
 *       "payload": { "name": "New Product Name" }
 *     },
 *     {
 *       "type": "UpdateProductDescription", 
 *       "payload": { "description": "Updated desc" }
 *     }
 *   ]
 * }
 * </pre>
 */
public record UpdateProductDto(String id, UpdateOperation[] operations) {

    /**
     * Interface polymorphique des opérations (Jackson polymorphic deserialization).
     *
     * <p>@JsonTypeInfo : discrimine par champ "type" → concrete impl</p>
     */
    @JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.EXISTING_PROPERTY,
        property = "type",
        visible = true
    )
    @JsonSubTypes({
        @JsonSubTypes.Type(value = UpdateNameOperation.class, name = "UpdateProductName"),
        @JsonSubTypes.Type(value = UpdateDescriptionOperation.class, name = "UpdateProductDescription")
    })
    public interface UpdateOperation {
        /** Type discriminant pour polymorphisme */
        UpdateProductOperationType type();
    }

    /**
     * Mise à jour nom produit.
     */
    @JsonTypeName("UpdateProductName")
    public record UpdateNameOperation(
            UpdateProductOperationType type, 
            UpdateNameOperationPayload payload) implements UpdateOperation {
        
        public record UpdateNameOperationPayload(String name) {}
    }

    /**
     * Mise à jour description produit.
     */
    @JsonTypeName("UpdateProductDescription")
    public record UpdateDescriptionOperation(
            UpdateProductOperationType type, 
            UpdateDescriptionOperationPayload payload) implements UpdateOperation {
        
        public record UpdateDescriptionOperationPayload(String description) {}
    }

    /**
     * Enum des types d'opérations (JSON @JsonProperty mapping).
     */
    public enum UpdateProductOperationType {
        @JsonProperty("UpdateProductName") UPDATE_NAME,
        @JsonProperty("UpdateProductDescription") UPDATE_DESCRIPTION;
    }
}
