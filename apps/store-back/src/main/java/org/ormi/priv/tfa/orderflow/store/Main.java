package org.ormi.priv.tfa.orderflow.store;

import io.quarkus.runtime.Quarkus;
import io.quarkus.runtime.QuarkusApplication;
import io.quarkus.runtime.annotations.QuarkusMain;

/**
 * Point d'entrée principal pour l'application Order Flow Store.
 * 
 * Cette classe initialise et exécute une application Quarkus qui gère le domaine du registre des produits.
 * Elle utilise le framework Quarkus pour démarrer et gérer le cycle de vie de l'application.
 * 
 * <p>L'application commence par exécuter la {@link ProductRegistryDomainApplication}
 * qui implémente {@link QuarkusApplication} et attend que l'application se termine.</p>
 * 
 * @version 1.0
 * @since 1.0
 */

@QuarkusMain
public class Main {

    public static void main(String... args) {
        Quarkus.run(
            ProductRegistryDomainApplication.class,
            (exitCode, exception) -> {},
            args);
    }

    public static class ProductRegistryDomainApplication implements QuarkusApplication {

        @Override
        public int run(String... args) throws Exception {
            Quarkus.waitForExit();
            return 0;
        }
    }
}
