package org.ormi.priv.tfa.orderflow.productregistry.application;

import org.ormi.priv.tfa.orderflow.kernel.product.ProductId;
import org.ormi.priv.tfa.orderflow.kernel.product.SkuId;

/**
 * Interface scellée définissant les commandes métier pour la gestion des produits dans le registre.
 * <p>
 * Toutes les commandes d'un produit implémentent cette interface pour assurer une cohérence
 * dans le traitement des opérations de création, mise à jour et suppression des produits.
 */
public sealed interface ProductCommand 
        permits RegisterProductCommand, RetireProductCommand, 
                UpdateProductNameCommand, UpdateProductDescriptionCommand {
    
    /**
     * Commande pour enregistrer un nouveau produit dans le registre.
     * <p>
     * Cette commande crée un nouveau produit avec ses informations de base et son identifiant SKU.
     *
     * @param name nom du produit
     * @param description description détaillée du produit
     * @param skuId identifiant unique du SKU associé au produit
     */
    public record RegisterProductCommand(
            String name,
            String description,
            SkuId skuId) implements ProductCommand {
    }

    /**
     * Commande pour retirer définitivement un produit du registre.
     *
     * @param productId identifiant unique du produit à retirer
     */
    public record RetireProductCommand(ProductId productId) implements ProductCommand {
    }

    /**
     * Commande pour mettre à jour le nom d'un produit existant.
     *
     * @param productId identifiant unique du produit à modifier
     * @param newName nouveau nom du produit
     */
    public record UpdateProductNameCommand(ProductId productId, String newName) implements ProductCommand {
    }

    /**
     * Commande pour mettre à jour la description d'un produit existant.
     *
     * @param productId identifiant unique du produit à modifier
     * @param newDescription nouvelle description du produit
     */
    public record UpdateProductDescriptionCommand(ProductId productId, String newDescription) implements ProductCommand {
    }
}
