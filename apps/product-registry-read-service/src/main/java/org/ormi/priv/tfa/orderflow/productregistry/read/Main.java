package org.ormi.priv.tfa.orderflow.productregistry.read;

import io.quarkus.runtime.Quarkus;
import io.quarkus.runtime.QuarkusApplication;
import io.quarkus.runtime.annotations.QuarkusMain;

/**
 * Point d'entrée principal de l'application Quarkus pour le module Product Registry Read.
 *
 * <p>Cette classe utilise l'annotation {@link QuarkusMain} pour définir un point
 * d'exécution personnalisé de l'application basée sur Quarkus. Elle délègue
 * le démarrage effectif à la classe interne {@link ProductRegistryReadApplication},
 * qui attend la fermeture du processus Quarkus pour terminer proprement.</p>
 *
 * <p>Le module <b>Product Registry Read</b> est responsable de la lecture et
 * de la consultation des données du registre de produits dans le cadre du
 * flux de commandes (Order Flow).</p>
 *
 */
@QuarkusMain
public class Main {

    /**
     * Point d'entrée standard de l'application Java.
     *
     * <p>Cette méthode initialise et exécute l'application Quarkus en
     * appelant {@link Quarkus#run(Class, java.util.function.BiConsumer, String...)},
     * avec {@link ProductRegistryReadApplication} comme classe principale.</p>
     *
     * @param args les arguments de ligne de commande passés à l'application.
     */
    public static void main(String... args) {
        Quarkus.run(
            ProductRegistryReadApplication.class,
            (exitCode, exception) -> {},
            args
        );
    }

    /**
     * Implémentation principale de l'application Quarkus.
     *
     * <p>Cette classe est exécutée au lancement par {@link Quarkus#run}.
     * Elle bloque le thread principal à l'aide de {@link Quarkus#waitForExit()}
     * jusqu'à ce que le processus Quarkus soit arrêté (par signal ou par arrêt explicite).</p>
     */
    public static class ProductRegistryReadApplication implements QuarkusApplication {

        /**
         * Exécute l'application Quarkus et attend sa terminaison.
         *
         * @param args les arguments de ligne de commande.
         * @return un code de sortie (0 en cas de succès).
         * @throws Exception si une erreur se produit pendant l'exécution.
         */
        @Override
        public int run(String... args) throws Exception {
            Quarkus.waitForExit();
            return 0;
        }
    }
}
