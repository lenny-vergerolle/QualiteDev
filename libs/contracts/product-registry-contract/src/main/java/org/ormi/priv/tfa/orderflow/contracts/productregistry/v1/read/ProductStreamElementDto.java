package org.ormi.priv.tfa.orderflow.contracts.productregistry.v1.read;

import java.time.Instant;

/**
 * DTO événement produit pour streaming SSE (Server-Sent Events).
 *
 * <p>Éléments légers diffusés en temps réel via ProductEventBroadcaster.
 * Utilisé par les endpoints de streaming (WebSocket/SSE).</p>
 *
 * <h3>Champs</h3>
 * <table>
 *   <tr><th>Champ</th><th>Type</th><th>Description</th></tr>
 *   <tr><td>type</td><td>String</td><td>ProductEventType (REGISTERED, NAME_UPDATED...)</td></tr>
 *   <tr><td>productId</td><td>String</td><td>UUID stringifié</td></tr>
 *   <tr><td>occuredAt</td><td>Instant</td><td>Timestamp événement</td></tr>
 * </table>
 *
 * <h3>Exemple JSON (SSE)</h3>
 * <pre>
 * {
 *   "type": "PRODUCT_NAME_UPDATED",
 *   "productId": "550e8400-e29b-41d4-a716-446655440000",
 *   "occuredAt": "2026-01-05T11:22:00Z"
 * }
 * </pre>
 *
 * <h3>Utilisation</h3>
 * <ul>
 *   <li>ProductEventBroadcaster.broadcast() → ce DTO</li>
 *   <li>ProductStreamResource (SSE endpoint)</li>
 * </ul>
 */
public record ProductStreamElementDto(
    String type,
    String productId,
    Instant occuredAt
) {
}
