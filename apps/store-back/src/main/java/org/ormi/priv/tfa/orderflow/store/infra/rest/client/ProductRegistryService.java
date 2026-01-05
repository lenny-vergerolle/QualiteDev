package org.ormi.priv.tfa.orderflow.store.infra.rest.client;

import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;
import org.jboss.resteasy.reactive.RestResponse;
import org.ormi.priv.tfa.orderflow.contracts.productregistry.v1.read.PaginatedProductListDto;
import org.ormi.priv.tfa.orderflow.contracts.productregistry.v1.read.ProductViewDto;

import io.smallrye.mutiny.Multi;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;

/**
 * Client REST MicroProfile vers Product Registry Read API (Query Side).
 *
 * <p>Proxy pour les opérations de lecture (search/view/stream).
 * Injecté via @RestClient dans ProductRpcResource.</p>
 *
 * <h3>Configuration</h3>
 * <ul>
 *   <li>@RegisterRestClient(configKey = "product-registry-read-api")</li>
 * </ul>
 *
 * <h3>Endpoints proxys</h3>
 * <table>
 *   <tr><th>Opération</th><th>HTTP</th><th>Path</th></tr>
 *   <tr><td>searchProducts</td><td>GET</td><td>/products?sku=&page=&size=</td></tr>
 *   <tr><td>getProductById</td><td>GET</td><td>/products/{id}</td></tr>
 *   <tr><td>streamProductEventsById</td><td>GET</td><td>/products/{id}/pending/stream (Ex5)</td></tr>
 * </table>
 */
@ApplicationScoped
@Path("/products")
@RegisterRestClient(configKey = "product-registry-read-api")
public interface ProductRegistryService {

    /**
     * Recherche paginée produits par pattern SKU.
     *
     * <p>Query params optionnels, pagination zero-based.</p>
     */
    @GET
    RestResponse<PaginatedProductListDto> searchProducts(
            @QueryParam("sku") String sku,
            @QueryParam("page") int page,
            @QueryParam("size") int size);

    /**
     * Consultation produit par ID.
     *
     * <p>Retourne ProductViewDto complet (200 OK).</p>
     */
    @GET @Path("/{id}")
    RestResponse<ProductViewDto> getProductById(@PathParam("id") String id);

    /**
     * TODO: implement [Exercice 5]
     * Streaming SSE événements produit (Mutiny Multi).
     *
     * <pre>GET /products/{id}/pending/stream</pre>
     *
     * @return Multi&lt;ProductStreamElementDto&gt; (Server-Sent Events JSON)
     */
    // @GET @Path("/{id}/pending/stream")
    // @Produces(MediaType.SERVER_SENT_EVENTS)
    // @RestStreamElementType(MediaType.APPLICATION_JSON)
    // Multi<ProductStreamElementDto> streamProductEventsByProductId(@PathParam("id") String productId);
}
