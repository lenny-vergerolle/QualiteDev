package org.ormi.priv.tfa.orderflow.productregistry.read.infra.api;

import jakarta.ws.rs.Path;

/**
 * API REST réactive pour le streaming d'événements produits.
 *
 * <p>Fournit des endpoints Server-Sent Events (SSE) pour le streaming en temps réel
 * des événements de produits via Mutiny Multi.</p>
 *
 * <h3>Endpoints (Exercice 5)</h3>
 * <ul>
 *   <li>GET /products/{id}/pending/stream - Événements pending d'un produit</li>
 * </ul>
 *
 * <h3>Technologies</h3>
 * <ul>
 *   <li>@RestStreamElementType(JSON) pour SSE JSON</li>
 *   <li>Mutiny Multi&lt;ProductStreamElementDto&gt;</li>
 * </ul>
 */
@Path("/products")
public class ProductStreamResource {
    
    // TODO: implement [Exercice 5]
    // private final ReadProductService readProductService;
    // private final ProductIdMapper productIdMapper;

    // @Inject
    // public ProductStreamResource(
    //         ReadProductService readProductService,
    //         ProductIdMapper productIdMapper) {
    //     this.readProductService = readProductService;
    //     this.productIdMapper = productIdMapper;
    // }

    // TODO: implement [Exercice 5]
    // @GET
    // @Path("/{id}/pending/stream")
    // @RestStreamElementType(MediaType.APPLICATION_JSON)
    // public Multi<ProductStreamElementDto> streamPendingOutboxMessagesByProdutId(
    //         @PathParam("id") String id) {
    //     return readProductService.streamProductEvents(productIdMapper.map(UUID.fromString(id)));
    // }
}
