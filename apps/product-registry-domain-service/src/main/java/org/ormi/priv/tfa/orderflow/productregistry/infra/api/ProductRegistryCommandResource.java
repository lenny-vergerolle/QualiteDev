package org.ormi.priv.tfa.orderflow.productregistry.infra.api;

import java.net.URI;
import java.util.UUID;

import org.jboss.resteasy.reactive.RestResponse;
import org.ormi.priv.tfa.orderflow.contracts.productregistry.v1.write.RegisterProductCommandDto;
import org.ormi.priv.tfa.orderflow.contracts.productregistry.v1.write.UpdateProductDescriptionParamsDto;
import org.ormi.priv.tfa.orderflow.contracts.productregistry.v1.write.UpdateProductNameParamsDto;
import org.ormi.priv.tfa.orderflow.kernel.product.ProductId;
import org.ormi.priv.tfa.orderflow.productregistry.application.ProductCommand.RetireProductCommand;
import org.ormi.priv.tfa.orderflow.productregistry.application.ProductCommand.UpdateProductDescriptionCommand;
import org.ormi.priv.tfa.orderflow.productregistry.application.ProductCommand.UpdateProductNameCommand;
import org.ormi.priv.tfa.orderflow.productregistry.application.RegisterProductService;
import org.ormi.priv.tfa.orderflow.productregistry.application.RetireProductService;
import org.ormi.priv.tfa.orderflow.productregistry.application.UpdateProductService;
import org.ormi.priv.tfa.orderflow.productregistry.infra.web.dto.CommandDtoMapper;

import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.PATCH;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.UriInfo;

/**
 * API REST pour les commandes de gestion du registre des produits.
 * <p>
 * Fournit les endpoints pour créer, retirer et mettre à jour les produits via
 * les services métier correspondants. Utilise le pattern CQRS avec mapping DTO.
 */
@Path("/products")
@Produces(MediaType.APPLICATION_JSON)
public class ProductRegistryCommandResource {

    private final CommandDtoMapper mapper;
    private final RegisterProductService registerProductService;
    private final RetireProductService retireProductService;
    private final UpdateProductService updateProductService;

    /**
     * Constructeur par injection de dépendances.
     *
     * @param mapper mapper entre DTO et commandes domaine
     * @param registerProductService service d'enregistrement de produits
     * @param retireProductService service de mise à la retraite de produits
     * @param updateProductService service de mise à jour de produits
     */
    @Inject
    public ProductRegistryCommandResource(
            CommandDtoMapper mapper,
            RegisterProductService registerProductService,
            RetireProductService retireProductService,
            UpdateProductService updateProductService) {
        this.mapper = mapper;
        this.registerProductService = registerProductService;
        this.retireProductService = retireProductService;
        this.updateProductService = updateProductService;
    }

    /**
     * Enregistre un nouveau produit.
     * <p>
     * Crée le produit et retourne un code 201 avec l'URI de la ressource créée.
     *
     * @param cmd DTO contenant les informations du produit
     * @param uriInfo informations d'URI pour construire la réponse de création
     * @return réponse 201 avec l'URI du produit créé
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public RestResponse<Void> registerProduct(RegisterProductCommandDto cmd, @Context UriInfo uriInfo) {
        final ProductId productId = registerProductService.handle(mapper.toCommand(cmd));
        return RestResponse.created(
                URI.create(uriInfo.getAbsolutePathBuilder().path("/products/" + productId.value()).build().toString()));
    }

    /**
     * Met à la retraite un produit existant.
     *
     * @param productId identifiant UUID du produit à retirer
     * @return réponse 204 (no content)
     */
    @DELETE
    @Path("/{id}")
    public RestResponse<Void> retireProduct(@PathParam("id") String productId) {
        retireProductService.retire(new RetireProductCommand(new ProductId(UUID.fromString(productId))));
        return RestResponse.noContent();
    }

    /**
     * Met à jour le nom d'un produit existant.
     *
     * @param productId identifiant UUID du produit
     * @param params DTO contenant le nouveau nom
     * @return réponse 204 (no content)
     */
    @PATCH
    @Path("/{id}/name")
    @Consumes(MediaType.APPLICATION_JSON)
    public RestResponse<Void> updateProductName(@PathParam("id") String productId, UpdateProductNameParamsDto params) {
        updateProductService
                .handle(new UpdateProductNameCommand(new ProductId(UUID.fromString(productId)), params.name()));
        return RestResponse.noContent();
    }

    /**
     * Met à jour la description d'un produit existant.
     *
     * @param productId identifiant UUID du produit
     * @param params DTO contenant la nouvelle description
     * @return réponse 204 (no content)
     */
    @PATCH
    @Path("/{id}/description")
    @Consumes(MediaType.APPLICATION_JSON)
    public RestResponse<Void> updateProductDescription(@PathParam("id") String productId,
            UpdateProductDescriptionParamsDto params) {
        updateProductService.handle(new UpdateProductDescriptionCommand(new ProductId(UUID.fromString(productId)),
                params.description()));
        return RestResponse.noContent();
    }
}
