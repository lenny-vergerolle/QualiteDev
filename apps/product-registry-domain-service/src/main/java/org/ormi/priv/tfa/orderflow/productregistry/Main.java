package org.ormi.priv.tfa.orderflow.productregistry;

import io.quarkus.runtime.Quarkus;
import io.quarkus.runtime.QuarkusApplication;
import io.quarkus.runtime.annotations.QuarkusMain;

/**
 * Point d'entrée principal de l'application Quarkus pour le registre des produits.
 * <p>
 * Cette classe lance l'application Quarkus en utilisant l'annotation {@link QuarkusMain}
 * et définit une implémentation personnalisée de {@link QuarkusApplication} pour le
 * domaine du registre des produits (ProductRegistryDomainApplication).
 */
@QuarkusMain
public class Main {

    /**
     * Lance l'application Quarkus pour le registre des produits.
     *
     * @param args arguments de ligne de commande passés à Quarkus
     */
    public static void main(String... args) {
        Quarkus.run(
            ProductRegistryDomainApplication.class,
            (exitCode, exception) -> {},
            args);
    }

    /**
     * Implémentation personnalisée de l'application Quarkus pour le registre des produits.
     * <p>
     * Cette classe interne gère le cycle de vie de l'application en attendant
     * l'arrêt explicite via Quarkus.waitForExit().
     */
    public static class ProductRegistryDomainApplication implements QuarkusApplication {

        /**
         * Point d'entrée principal de l'application Quarkus.
         * <p>
         * Attend indéfiniment l'arrêt de l'application via Quarkus.waitForExit().
         *
         * @param args arguments de ligne de commande
         * @return code de sortie 0 (succès)
         * @throws Exception en cas d'erreur lors de l'exécution
         */
        @Override
        public int run(String... args) throws Exception {
            Quarkus.waitForExit();
            return 0;
        }
    }
}
