package org.ormi.priv.tfa.orderflow.kernel.product;

import jakarta.validation.constraints.NotNull;

import java.util.regex.Pattern;

/**
 * Value Object SKU Identifiant (format strict AAA-12345).
 *
 * <p>Wrapper validé autour String avec regex canonique :
 * <ul>
 *   <li>AAA : 3 uppercase lettres</li>
 *   <li>-</li>
 *   <li>12345 : 5 chiffres</li>
 * </ul></p>
 *
 * <h3>Validation</h3>
 * <table>
 *   <tr><th>Valide</th><th>Invalide</th></tr>
 *   <tr><td>ABC-12345</td><td>ab-123, ABC-1234, ABC1-12345</td></tr>
 * </table>
 *
 * <h3>Compact Constructor</h3>
 * <p>Validation runtime + @NotNull Bean Validation.</p>
 */
public record SkuId(@NotNull String value) {
    
    /** Regex SKU : AAA-DDDDD */
    private static final Pattern SKU_PATTERN = 
        Pattern.compile("^[A-Z]{3}-\\d{5}$");

    /**
     * Validateur compact constructor (runtime invariant).
     */
    public SkuId {
        if (!SKU_PATTERN.matcher(value).matches()) {
            throw new IllegalArgumentException("Invalid SKU format, expected [A-Z]{3}-[0-9]{5}");
        }
    }
}
