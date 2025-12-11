package org.ormi.priv.tfa.orderflow.productregistry.infra.web.dto;

import org.mapstruct.Builder;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import org.ormi.priv.tfa.orderflow.productregistry.application.ProductCommand.RegisterProductCommand;
import org.ormi.priv.tfa.orderflow.contracts.productregistry.v1.write.RegisterProductCommandDto;
import org.ormi.priv.tfa.orderflow.kernel.product.SkuIdMapper;

/**
 * Mapper MapStruct pour la conversion entre les DTO web REST et les commandes domaine.
 * <p>
 * Convertit bidirectionnellement les DTO de la version 1 de l'API contrat vers la commande
 * {@link RegisterProductCommand} pour l'enregistrement de produits, en utilisant le mapper SkuId.
 */
@Mapper(
    componentModel = "cdi",
    builder = @Builder(disableBuilder = true),
    uses = { SkuIdMapper.class },
    unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface CommandDtoMapper {
    
    /**
     * Convertit un DTO web en commande domaine pour l'enregistrement d'un produit.
     *
     * @param dto DTO entrant depuis l'API REST
     * @return commande domaine prête pour le service métier
     */
    RegisterProductCommand toCommand(RegisterProductCommandDto dto);
    
    /**
     * Convertit une commande domaine en DTO web pour la réponse REST.
     *
     * @param command commande domaine source
     * @return DTO prêt pour la sérialisation JSON
     */
    RegisterProductCommandDto toDto(RegisterProductCommand command);
}
