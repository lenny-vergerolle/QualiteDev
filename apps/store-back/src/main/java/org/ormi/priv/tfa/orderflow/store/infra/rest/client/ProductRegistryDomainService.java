package org.ormi.priv.tfa.orderflow.store.infra.rest.client;

import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;
import org.jboss.resteasy.reactive.RestResponse;
import org.ormi.priv.tfa.orderflow.contracts.productregistry.v1.write.RegisterProductCommandDto;
import org.ormi.priv.tfa.orderflow.contracts.productregistry.v1.write.UpdateProductDescriptionParamsDto;
import org.ormi.priv.tfa.orderflow.contracts.productregistry.v1.write.UpdateProductNameParamsDto;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.PATCH;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;

/**
 * Client REST MicroProfile vers Product Registry Domain API (Write Side).
 *
 * <p>Interface proxy pour les commandes CQRS (Register/Retire/Update).
 * Injecté via @RestClient dans ProductRpcResource.</p>
 *
 * <h3>Configuration</h3>
 * <ul>
 *   <li>@RegisterRestClient(configKey = "product-registry-api")</li>
 *   <li>application.properties : quarkus.rest-client."product-registry-api".url</li>
 * </ul>
 *
 * <h3>Endpoints proxys</h3>
 * <table>
 *   <tr><th>Opération</th><th>HTTP</th><th>Path</th></tr>
 *   <tr><td>registerProduct</td><td>POST</td><td>/products</td></tr>
 *   <tr><td>retireProduct</td><td>DELETE</td><td>/products/{id}</td></tr>
 *   <tr><td>updateProductName</td><td>PATCH</td><td>/products/{id}/name</td></tr>
 *   <tr><td>updateProductDescription</td><td>PATCH</td><td>/products/{id}/description</td></tr>
 * </table>
 */
@ApplicationScoped
@Path("/products")
@RegisterRestClient(configKey = "product-registry-api")
public interface ProductRegistryDomainService {
    
    /**
     * Enregistrement nouveau produit (CQRS Command).
     *
     * <p>Retourne 201 Created → ok(), sinon 4xx → BAD_REQUEST</p>
     */
    @POST
    RestResponse<Void> registerProduct(RegisterProductCommandDto cmd);

    /**
     * Retraite produit (soft delete).
     *
     * <p>Retourne 204 No Content → ok(), sinon erreur</p>
     */
    @DELETE @Path("/{id}")
    RestResponse<Void> retireProduct(@PathParam("id") String productId);

    /**
     * Mise à jour nom produit.
     *
     * <p>Retourne 204 No Content → ok()</p>
     */
    @PATCH @Path("/{id}/name")
    RestResponse<Void> updateProductName(
            @PathParam("id") String productId, 
            UpdateProductNameParamsDto params);

    /**
     * Mise à jour description produit.
     *
     * <p>Retourne 204 No Content → ok()</p>
     */
    @PATCH @Path("/{id}/description")
    RestResponse<Void> updateProductDescription(
            @PathParam("id") String productId, 
            UpdateProductDescriptionParamsDto params);
}
