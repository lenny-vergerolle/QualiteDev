package org.ormi.priv.tfa.orderflow.productregistry.read.infra.web.dto;

import org.mapstruct.Builder;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.ValueMapping;
import org.mapstruct.ValueMappings;
import org.ormi.priv.tfa.orderflow.contracts.productregistry.v1.read.ProductViewDto;
import org.ormi.priv.tfa.orderflow.contracts.productregistry.v1.read.ProductViewDto.ProductViewDtoCatalog;
import org.ormi.priv.tfa.orderflow.contracts.productregistry.v1.read.ProductViewDto.ProductViewDtoEvent;
import org.ormi.priv.tfa.orderflow.contracts.productregistry.v1.read.ProductViewDto.ProductViewDtoEventType;
import org.ormi.priv.tfa.orderflow.contracts.productregistry.v1.read.ProductViewDto.ProductViewEventDtoPayload;
import org.ormi.priv.tfa.orderflow.contracts.productregistry.v1.read.ProductViewDto.ProductViewEventDtoPayload.ProductDescriptionUpdatedPayloadDto;
import org.ormi.priv.tfa.orderflow.contracts.productregistry.v1.read.ProductViewDto.ProductViewEventDtoPayload.ProductNameUpdatedPayloadDto;
import org.ormi.priv.tfa.orderflow.contracts.productregistry.v1.read.ProductViewDto.ProductViewEventDtoPayload.ProductRegisteredPayloadDto;
import org.ormi.priv.tfa.orderflow.contracts.productregistry.v1.read.ProductViewDto.ProductViewEventDtoPayload.ProductRetiredPayloadDto;
import org.ormi.priv.tfa.orderflow.cqrs.DomainEvent.DomainEventPayload;
import org.ormi.priv.tfa.orderflow.kernel.product.ProductEventV1.ProductDescriptionUpdated.ProductDescriptionUpdatedPayload;
import org.ormi.priv.tfa.orderflow.kernel.product.ProductEventV1.ProductEventV1Payload;
import org.ormi.priv.tfa.orderflow.kernel.product.ProductEventV1.ProductEventV1Payload.Empty;
import org.ormi.priv.tfa.orderflow.kernel.product.ProductEventV1.ProductNameUpdated.ProductNameUpdatedPayload;
import org.ormi.priv.tfa.orderflow.kernel.product.ProductEventV1.ProductRegistered.ProductRegisteredPayload;
import org.ormi.priv.tfa.orderflow.kernel.product.ProductIdMapper;
import org.ormi.priv.tfa.orderflow.kernel.product.SkuIdMapper;
import org.ormi.priv.tfa.orderflow.kernel.product.views.ProductEventType;
import org.ormi.priv.tfa.orderflow.kernel.product.views.ProductView;
import org.ormi.priv.tfa.orderflow.kernel.product.views.ProductView.ProductViewCatalogRef;
import org.ormi.priv.tfa.orderflow.kernel.product.views.ProductView.ProductViewEvent;

/**
 * Mapper MapStruct Domain → REST DTO (ProductView → ProductViewDto v1).
 *
 * <p>Hiérarchie imbriquée de mappers pour gérer :
 * <ul>
 *   <li>ProductView → ProductViewDto</li>
 *   <li>ProductViewEvent → ProductViewDtoEvent + payloads polymorphiques</li>
 *   <li>ProductViewCatalogRef → ProductViewDtoCatalog</li>
 *   <li>ProductEventType → ProductViewDtoEventType (@ValueMappings)</li>
 * </ul></p>
 *
 * <h3>Pattern clé : Dispatcher polymorphique</h3>
 * <p>Pour interface→interface : <code>DomainEventPayload → ProductViewEventDtoPayload</code></p>
 */
@Mapper(componentModel = "cdi", 
        builder = @Builder(disableBuilder = false), 
        uses = {
            ProductIdMapper.class,
            SkuIdMapper.class,
            ProductViewDtoMapper.ProductViewDtoEventMapper.class,
            ProductViewDtoMapper.ProductViewDtoCatalogMapper.class
        }, 
        unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ProductViewDtoMapper {
    
    /**
     * Mapping principal : Domain → REST DTO.
     */
    ProductViewDto toDto(ProductView productView);

    /**
     * Mapper imbriqué : événements avec payloads polymorphiques.
     */
    @Mapper(componentModel = "cdi", 
            builder = @Builder(disableBuilder = false), 
            unmappedTargetPolicy = ReportingPolicy.IGNORE, 
            uses = {
                ProductEventTypeMapper.class, 
                ProductViewDtoEventMapper.ProductViewDtoPayloadMapper.class
            })
    interface ProductViewDtoEventMapper {
        
        /**
         * ProductViewEvent → ProductViewDtoEvent (avec type + payload).
         */
        ProductViewDtoEvent toDto(ProductViewEvent event);

        /**
         * Mapper imbriqué : dispatch payloads polymorphiques.
         */
        @Mapper(componentModel = "cdi", unmappedTargetPolicy = ReportingPolicy.IGNORE)
        interface ProductViewDtoPayloadMapper {
            
            ProductRegisteredPayloadDto toDto(ProductRegisteredPayload payload);
            ProductNameUpdatedPayloadDto toDto(ProductNameUpdatedPayload payload);
            ProductDescriptionUpdatedPayloadDto toDto(ProductDescriptionUpdatedPayload payload);
            ProductRetiredPayloadDto toDto(Empty payload);

            /**
             * 👇 DISPATCHER polymorphique (interface → interface)
             * MapStruct génère un switch pattern matching.
             */
            default ProductViewEventDtoPayload map(DomainEventPayload payload) {
                if (payload == null) return null;
                if (payload instanceof ProductRegisteredPayload p) return toDto(p);
                if (payload instanceof ProductNameUpdatedPayload p) return toDto(p);
                if (payload instanceof ProductDescriptionUpdatedPayload p) return toDto(p);
                if (payload instanceof ProductEventV1Payload.Empty p) return toDto(p);
                throw new IllegalArgumentException("Unknown payload type: " + payload.getClass());
            }
        }
    }

    /**
     * Mapper catalogues (simple record → record).
     */
    @Mapper(componentModel = "cdi", 
            builder = @Builder(disableBuilder = false), 
            unmappedTargetPolicy = ReportingPolicy.IGNORE)
    interface ProductViewDtoCatalogMapper {
        ProductViewDtoCatalog toDto(ProductViewCatalogRef catalog);
    }

    /**
     * Mapper enum ProductEventType → ProductViewDtoEventType.
     */
    @Mapper(componentModel = "cdi", 
            builder = @Builder(disableBuilder = false), 
            unmappedTargetPolicy = ReportingPolicy.IGNORE)
    interface ProductEventTypeMapper {
        @ValueMappings({
            @ValueMapping(source = "PRODUCT_REGISTERED", target = "REGISTERED"),
            @ValueMapping(source = "PRODUCT_NAME_UPDATED", target = "NAME_UPDATED"),
            @ValueMapping(source = "PRODUCT_DESCRIPTION_UPDATED", target = "DESCRIPTION_UPDATED"),
            @ValueMapping(source = "PRODUCT_RETIRED", target = "RETIRED")
        })
        ProductViewDtoEventType toDto(ProductEventType event);
    }
}
