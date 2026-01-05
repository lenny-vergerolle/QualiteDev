package org.ormi.priv.tfa.orderflow.store.infra.api;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.jboss.resteasy.reactive.RestResponse;
import org.ormi.priv.tfa.orderflow.contracts.productregistry.v1.read.PaginatedProductListDto;
import org.ormi.priv.tfa.orderflow.contracts.productregistry.v1.read.ProductViewDto;
import org.ormi.priv.tfa.orderflow.contracts.productregistry.v1.write.RegisterProductCommandDto;
import org.ormi.priv.tfa.orderflow.contracts.productregistry.v1.write.UpdateProductDescriptionParamsDto;
import org.ormi.priv.tfa.orderflow.contracts.productregistry.v1.write.UpdateProductNameParamsDto;
import org.ormi.priv.tfa.orderflow.store.infra.api.dto.RetireProductDto;
import org.ormi.priv.tfa.orderflow.store.infra.api.dto.SearchProductsDto;
import org.ormi.priv.tfa.orderflow.store.infra.api.dto.UpdateProductDto;
import org.ormi.priv.tfa.orderflow.store.infra.api.dto.ViewProductDto;
import org.ormi.priv.tfa.orderflow.store.infra.rest.client.ProductRegistryDomainService;
import org.ormi.priv.tfa.orderflow.store.infra.rest.client.ProductRegistryService;

import io.smallrye.common.annotation.Blocking;
import io.smallrye.mutiny.Uni;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response.Status;

/**
 * API RPC/Proxy vers Product Registry (Store → Registry).
 *
 * <p>Facade POST-only pour orchestrer les appels REST clients vers :
 * <ul>
 *   <li>ProductRegistryService (read)</li>
 *   <li>ProductRegistryDomainService (write/CQRS commands)</li>
 * </ul></p>
 *
 * <h3>Pattern : POST Resource Pattern</h3>
 * <p>Tous les endpoints POST (même GET-like) pour uniformité + body validation.</p>
 *
 * <h3>Clients injectés (@RestClient)</h3>
 * <table>
 *   <tr><th>Client</th><th>Usage</th></tr>
 *   <tr><td>ProductRegistryService</td><td>Read (view/search)</td></tr>
 *   <tr><td>ProductRegistryDomainService</td><td>Write (commands)</td></tr>
 * </table>
 */
@Path("/products")
@Produces(MediaType.APPLICATION_JSON)
public class ProductRpcResource {

    @Inject @RestClient private ProductRegistryService productRegistryService;
    @Inject @RestClient private ProductRegistryDomainService productRegistryDomainService;

    /**
     * Enregistrement produit (Command → Domain Service).
     *
     * <pre>POST /products/registerProduct</pre>
     */
    @POST @Path("/registerProduct")
    @Consumes(MediaType.APPLICATION_JSON)
    public RestResponse<Void> registerProduct(RegisterProductCommandDto product) {
        final var res = productRegistryDomainService.registerProduct(product);
        if (res.getStatus() == Status.CREATED.getStatusCode()) {
            return RestResponse.ok();
        } else {
            return RestResponse.status(Status.BAD_REQUEST);
        }
    }

    /**
     * Mise à jour multiple (batch operations) → fan-out parallèle.
     *
     * <p>Supporte UPDATE_NAME + UPDATE_DESCRIPTION en parallèle (Mutiny Uni.combine).</p>
     *
     * <pre>POST /products/updateProduct</pre>
     */
    @POST @Path("/updateProduct")
    @Consumes(MediaType.APPLICATION_JSON)
    @Blocking
    public Uni<RestResponse<Void>> updateProduct(UpdateProductDto update) {
        if (update.id() == null || update.id().isEmpty()) {
            return Uni.createFrom().item(RestResponse.status(Status.BAD_REQUEST));
        }
        if (update.operations() == null || update.operations().length == 0) {
            return Uni.createFrom().item(RestResponse.status(Status.BAD_REQUEST));
        }

        List<Uni<RestResponse<?>>> ops = new ArrayList<>();

        for (var op : update.operations()) {
            switch (op.type()) {
                case UPDATE_NAME -> {
                    var nameOp = (UpdateProductDto.UpdateNameOperation) op;
                    ops.add(Uni.createFrom().item(
                        productRegistryDomainService.updateProductName(update.id(),
                            new UpdateProductNameParamsDto(nameOp.payload().name()))));
                }
                case UPDATE_DESCRIPTION -> {
                    var descOp = (UpdateProductDto.UpdateDescriptionOperation) op;
                    ops.add(Uni.createFrom().item(
                        productRegistryDomainService.updateProductDescription(update.id(),
                            new UpdateProductDescriptionParamsDto(descOp.payload().description()))));
                }
            }
        }

        if (ops.isEmpty()) {
            return Uni.createFrom().item(RestResponse.status(Status.NOT_MODIFIED));
        }

        return Uni.combine().all().unis(ops).with(results -> {
            boolean allOk = results.stream()
                .map(r -> ((RestResponse<?>) r).getStatus())
                .allMatch(status -> status == Status.NO_CONTENT.getStatusCode());
            return allOk ? RestResponse.ok() : RestResponse.status(Status.INTERNAL_SERVER_ERROR);
        });
    }

    /**
     * Retraite produit (Command → Domain Service).
     *
     * <pre>POST /products/retireProduct</pre>
     */
    @POST @Path("/retireProduct")
    @Consumes(MediaType.APPLICATION_JSON)
    public RestResponse<Void> retireProduct(RetireProductDto retire) {
        if (retire.id() == null || retire.id().isEmpty()) {
            return RestResponse.status(Status.BAD_REQUEST);
        }
        final var res = productRegistryDomainService.retireProduct(retire.id());
        return res.getStatus() == Status.NO_CONTENT.getStatusCode() 
            ? RestResponse.ok() 
            : RestResponse.status(Status.INTERNAL_SERVER_ERROR);
    }

    /**
     * Consultation produit (Query → Read Service).
     *
     * <pre>POST /products/viewProduct</pre>
     */
    @POST @Path("/viewProduct")
    @Consumes(MediaType.APPLICATION_JSON)
    public RestResponse<ProductViewDto> viewProduct(ViewProductDto view) {
        if (view.id() == null || view.id().isEmpty()) {
            return RestResponse.status(Status.BAD_REQUEST);
        }
        final var res = productRegistryService.getProductById(view.id());
        return res.getStatus() == Status.OK.getStatusCode() 
            ? RestResponse.ok(res.getEntity()) 
            : RestResponse.status(Status.INTERNAL_SERVER_ERROR);
    }

    /**
     * Recherche paginée (Query → Read Service).
     *
     * <pre>POST /products/searchProducts</pre>
     */
    @POST @Path("/searchProducts")
    @Consumes(MediaType.APPLICATION_JSON)
    public RestResponse<PaginatedProductListDto> searchProducts(SearchProductsDto search) {
        final var res = productRegistryService.searchProducts(search.sku(), search.page(), search.size());
        return res.getStatus() == Status.OK.getStatusCode() 
            ? RestResponse.ok(res.getEntity()) 
            : RestResponse.status(Status.INTERNAL_SERVER_ERROR);
    }

    /**
     * TODO: implement [Exercice 5]
     * Streaming SSE événements produit (Query → Read Service).
     *
     * <pre>GET /products/{id}/streamProductEvents</pre>
     */
    // @GET @Path("/{id}/streamProductEvents")
    // @Produces(MediaType.SERVER_SENT_EVENTS)
    // @RestStreamElementType(MediaType.APPLICATION_JSON)
    // public Multi<ProductStreamElementDto> streamProductEventsById(@PathParam("id") String id) {
    //     return productRegistryService.streamProductEventsById(id);
    // }
}
