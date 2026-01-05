package org.ormi.priv.tfa.orderflow.kernel.product;

/**
 * Énumération cycle de vie produit (DDD Domain Enum).
 *
 * <h3>États</h3>
 * <table>
 *   <tr><th>État</th><th>Description</th><th>Transitions</th></tr>
 *   <tr><td>ACTIVE</td><td>Produit actif</td><td>→ RETIRED</td></tr>
 *   <tr><td>RETIRED</td><td>Produit retiré</td><td>-</td></tr>
 * </table>
 *
 * <h3>Guards métier (Product)</h3>
 * <ul>
 *   <li>updateName/desc : ACTIVE uniquement</li>
 *   <li>retire() : ACTIVE → RETIRED</li>
 * </ul>
 *
 * <h3>Persistance</h3>
 * <ul>
 *   <li>JPA @Enumerated(EnumType.STRING)</li>
 *   <li>JSON sérialisation native</li>
 * </ul>
 */
public enum ProductLifecycle {
    /** Produit actif (créé, modifiable) */
    ACTIVE,
    
    /** Produit retiré (read-only, historique) */
    RETIRED
}
