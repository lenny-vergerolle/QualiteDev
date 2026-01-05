package org.ormi.priv.tfa.orderflow.kernel.product.persistence;

import org.ormi.priv.tfa.orderflow.kernel.product.ProductEventV1;

/**
 * Versionning schéma événements produit (Event Schema Evolution).
 *
 * <p>Mappe versions logiques → versions numériques (EventLogEntity.event_version).</p>
 *
 * <h3>Support versions</h3>
 * <table>
 *   <tr><th>Version</th><th>Valeur</th><th>Événements</th></tr>
 *   <tr><td>V1</td><td>1</td><td>ProductRegistered, NameUpdated...</td></tr>
 * </table>
 *
 * <h3>Utilisation</h3>
 * <pre>
 * if (ev.getEventVersion() == ProductEventVersion.V1.getValue()) {
 *   dispatcher.dispatchV1(mapper.toV1(ev));
 * }
 * </pre>
 */
public enum ProductEventVersion {
    
    /** V1 : événements initiaux (ProductEventV1) */
    V1(ProductEventV1.EVENT_VERSION);

    private final int value;

    ProductEventVersion(int value) {
        this.value = value;
    }

    /**
     * Valeur numérique pour persistance.
     */
    public int getValue() {
        return value;
    }
}
