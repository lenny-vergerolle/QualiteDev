package org.ormi.priv.tfa.orderflow.productregistry.read.application;

import java.util.concurrent.CopyOnWriteArrayList;

import org.ormi.priv.tfa.orderflow.contracts.productregistry.v1.read.ProductStreamElementDto;

import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.subscription.MultiEmitter;
import jakarta.enterprise.context.ApplicationScoped;

/**
 * Broadcaster d'événements pour le flux de produits (Product Registry Read).
 *
 * <p>Implémente un pattern <b>Publish/Subscribe (Pub/Sub)</b> thread-safe utilisant
 * Mutiny pour diffuser les événements {@link ProductStreamElementDto} à de multiples
 * abonnés. Utilise {@link CopyOnWriteArrayList} pour gérer les émetteurs de manière
 * sûre dans un environnement multi-threadé.</p>
 *
 * <h3>Utilisation</h3>
 * <pre>
 * // Diffusion d'un événement
 * productEventBroadcaster.broadcast(productElement);
 *
 * // Souscription au flux
 * productEventBroadcaster.stream()
 *     .subscribe().with(
 *         element -&gt; process(element),
 *         failure -&gt; log.error(failure)
 *     );
 * </pre>
 *
 * @author [Ton nom]
 * @version 1.0
 * @since 2026-01-05
 */
@ApplicationScoped
public class ProductEventBroadcaster {

    /**
     * Liste thread-safe des émetteurs actifs.
     * Utilise CopyOnWriteArrayList pour éviter les ConcurrentModificationException
     * lors des itérations et suppressions concurrentes.
     */
    private final CopyOnWriteArrayList<MultiEmitter<? super ProductStreamElementDto>> emitters = 
        new CopyOnWriteArrayList<>();

    /**
     * Diffuse un élément de produit à tous les abonnés actifs.
     *
     * <p>L'élément est envoyé de manière synchrone à chaque émetteur connecté.
     * Si un émetteur échoue, les autres continuent de recevoir l'événement.</p>
     *
     * @param element l'élément de produit à diffuser
     */
    public void broadcast(ProductStreamElementDto element) {
        emitters.forEach(emitter -> emitter.emit(element));
    }

    /**
     * Crée un flux Mutiny diffusant tous les événements de produits.
     *
     * <p>Chaque appel retourne un nouveau {@link Multi} qui s'enregistre
     * automatiquement comme abonné. Le flux se termine quand l'abonné se désabonne.</p>
     *
     * <p><b>TODOs en cours :</b></p>
     * <ul>
     *   <li>Ajouter un log DEBUG lors de l'ajout d'un nouvel émetteur</li>
     *   <li>⚠️ Nettoyer les émetteurs terminés pour éviter la fuite mémoire</li>
     * </ul>
     *
     * @return un Multi diffusant tous les ProductStreamElementDto
     */
    public Multi<ProductStreamElementDto> stream() {
        return Multi.createFrom().emitter(emitter -> {
            emitters.add(emitter);
            // TODO: log a debug, "New emitter added" -> log.debug("New emitter added, total: {}", emitters.size());

            // TODO: Hey! remove emitters, my RAM is melting! (and log for debugging)
            emitter.onTermination(() -> {
                boolean removed = emitters.remove(emitter);
                // log.debug("Emitter removed, total: {}", emitters.size());
            });
        });
    }

    /**
     * TODO: implement [Exercice 5]
     * Flux filtré par identifiant de produit unique.
     *
     * @param productId l'ID du produit à filtrer
     * @return Multi contenant uniquement les événements du produit spécifié
     */
    // public Multi<ProductStreamElementDto> streamByProductId(String productId) {
    //     return stream()
    //         .filter(element -> productId.equals(element.getProductId()));
    // }

    /**
     * TODO: implement [Exercice 5]
     * Flux filtré par liste d'identifiants de produits.
     *
     * @param productIds liste des IDs de produits à filtrer
     * @return Multi contenant les événements des produits spécifiés
     */
    // public Multi<ProductStreamElementDto> streamByProductIds(List<String> productIds) {
    //     return stream()
    //         .filter(element -> productIds.contains(element.getProductId()));
    // }
}
