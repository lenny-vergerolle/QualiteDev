# Introduction : Lancer et tester l'application

Dans cette première étape, vous allez configurer votre environnement de développement et lancer l'application. Vous apprendrez également à tester les différentes fonctionnalités de l'application.

## Étapes à suivre

1. Cloner le dépôt Git contenant le code de l'application.
2. Installer les dépendances nécessaires.
3. Lancer l'application en mode développement.
4. Tester les fonctionnalités de l'application à l'aide des outils fournis.

## Résultats attendus

À la fin de cette étape, vous devriez être en mesure de lancer l'application et de tester ses fonctionnalités de base.

## Lancer les composants

La liste des composants à lancer est la suivante :

- Service de registre de produits
    - Commande (écriture) : `Product Registry Command Service`
        - Process Java sur le port 8091
    - Query (lecture) : `Product Registry Read Service`
        - Process Java sur le port 8092
- BFF (Backend For Frontend) : `Order Flow BFF`
    - Process Java sur le port 8080
- Front de gestion de magasin : `Store Front`
    - Process Node sur le port 4200

## Architecture simplifiée

Concrètement, dans cette architecture "Orientée Services" les composants intéragissent comme suit :

- Le **Store Front** envoie des requêtes au **Order Flow BFF** pour initier le processus de commande.
- Le **Front** et le **BFF** sont couplés 1 à 1 via une API : chaque fonctionnalité du **Front** correspond à un point d'entrée dans le **BFF**. Ainsi, modifier une fonctionnalité de manière atomique est facilité.
- Le **BFF** ne porte aucune logique métier : ce n'est pas son rôle. Cependant, il **peut effectuer des transformations de données** si nécessaire. Le BFF sert alors de passerelle et effectue une "composition des API".
- Le **BFF** communique avec le **Product Registry Domain Service** pour traîter les commandes (entendre "commande" au sens système/technique du terme, c'est à dire une instruction destinée à modifier l'état de l'application ["écrit cette donnée", "supprime cet élément", "met à jour cet élément", etc.]).
- Le **Product Registry Read Service** est utilisé pour récupérer les informations sur les produits disponibles. Il expose une API de lecture optimisée pour les requêtes fréquentes du couple front-end/BFF (ou pour d'autres besoins spécifiques liés à d'autres potentiels consommateurs).

## Jouer les migrations de la base de données

L'application repose sur liquibase pour gérer les migrations de la base de données. Avant de lancer l'application, vous devez exécuter les migrations pour créer les tables nécessaires dans la base de données.

Référez-vous au fichier `README.md` dans le répertoire `libs/sql` pour plus d'informations sur la façon d'exécuter les migrations.

:::tip
Le projet embarque un "sidecar" liquibase (conteneur dédié). Pour exécutez les commandes `liquibase`, vous devez vous situer dedans. Sur votre hôte, dans un terminal :
``` bash
docker exec -it <id_conteneur> bash
```

... ainsi vous accédez à un terminal `bash` dans le conteneur liquibase dans lequel le binaire et la config BDD sont déjà présents.
:::

## Lancer les différents modules

Pour lancer les différents modules de l'application, utilisez le script Gradle `quarkusDev` pour chaque module.

Exemple :

```bash
gradle :apps:product-registry-domain-service:quarkusDev
```

Lancer le front :

```bash
pnpm run --filter apps-store-front start
```

## Tester l'application

### Créer un produit

1. Ouvrez votre navigateur web et accédez à `http://localhost:4200`.
2. Cliquez sur "Register New Product".
3. Remplissez le formulaire avec les informations du produit (SKU, Nom, Description).
4. Cliquez sur "Register Prodcut" pour enregistrer le produit.

:::warning
La nature asynchrone de l'architecture peut entraîner un délai entre l'enregistrement du produit et sa disponibilité dans la liste des produits. Patientez quelques instants avant de vérifier la présence du produit dans la liste. En général, la projection ne dure que quelques secondes.
:::

### Lister les produits

1. Dans l'interface du Store Front, cliquez sur "Products". (ou rechargez la page si vous y êtes déjà)
2. Vous devriez voir le produit que vous venez de créer dans la liste des produits disponibles.

### Modifier un produit

1. Dans la liste des produits, cliquez sur le produit que vous souhaitez modifier.
2. Cliquez sur "Edit".
3. Modifiez les informations du produit dans le formulaire.
4. Cliquez sur "Save Changes" pour enregistrer les modifications.

:::info
Vous remarquerez que les modifications sont immédiatement visibles dans l'interface. Cependant, en arrière-plan, le système traite les événements de modification de manière asynchrone. C'est en fait le front qui met à jour l'affichage immédiatement pour une meilleure expérience utilisateur.
:::

:::warning
A contrario, la liste des évènements associés au produit n'est pas mise à jour immédiatement. Vous devrez patienter quelques instants avant de voir les nouveaux événements apparaître dans la liste des événements du produit après un rafraîchissement de la page.
:::

### Retirer un produit

1. Dans la liste des produits, cliquez sur le produit que vous souhaitez retirer.
2. Cliquez sur "Retire Product".
3. Confirmez l'action dans l'invite de confirmation.
4. Le produit sera retiré de la liste des produits disponibles.

### Vérifier l'état final dans la liste des produits

1. Revenez à la liste des produits dans l'interface du Store Front.
2. Vérifiez que le produit que vous avez retiré n'apparaît plus dans la liste des produits disponibles.

:::warning
Comme pour les autres opérations, le retrait du produit est traité de manière asynchrone. Patientez quelques instants avant de vérifier que le produit a bien été retiré de la liste.
:::

:::info
Vous remarquerez que le produit, bien que "supprimé" du registre, reste accessible via son identifiant direct (URL). Cela est dû au fait que le système conserve l'historique des produits pour des raisons d'audit et de traçabilité. Vous pouvez toujours consulter les détails du produit et son historique d'événements en accédant directement à son URL, ou en cliquant sur son entrée dans la liste.

Remarque : il n'est plus possible d'interagir avec le produit (modification, retrait) une fois qu'il a été retiré. De plus, son SKU reste réservé et ne peut pas être réutilisé pour un nouveau produit.
:::

### Observer le contenu de la base de données

Vous pouvez utiliser un outil de gestion de base de données comme DBeaver pour vous connecter à la base de données PostgreSQL et observer les tables et les données.

:::tip
Votre IDE est normalement pré-paramétré pour exposer le port 5432 en local, vous pouvez donc vous connecter à la base de données depuis votre machine hôte.
:::

1. Regardez le schéma `public` pour voir les tables créées par Liquibase.
    - `databasechangelog` : table de suivi des migrations Liquibase.
    - `databasechangeloglock` : table de verrouillage des migrations Liquibase.
2. Regardez le schéma `domain` pour voir les tables utilisées par l'application.
    - `products` : table principale des produits.
3. Regardez le schéma `eventing`:
    - `event_log` : table des événements produits par le domaine.
    - `outbox` : table de l'outbox pour la gestion des messages asynchrones.
4. Regardez le schéma `read_product_registry`:
    - `products_view` : table de lecture optimisée pour les produits.

:::tip
Vous remarquerez que la table `outbox` est vide. En effet, l'outbox ne contient en réalité que des données éphémères et transitoires. Les messages sont consommés rapidement après leur insertion, de sorte que la table reste généralement vide et le schéma de lecture `read_product_registry` est constamment mis à jour avec les dernières informations par l'application.
:::

## Gérer consommation de ressources de l'environnement de développement

### Limiter la consommation de ressources

Les environnements de développement Java peuvent être gourmands en ressources. Pour limiter la consommation de ressources de votre machine, vous pouvez ajuster les paramètres JVM dans les fichiers `gradle.properties` situés dans les répertoires des modules.

Il est aussi possible de définir des variables d'environnement pour gradle :

- Limiter la mémoire maximale allouée à la JVM Gradle :

```bash
export GRADLE_OPTS="-Xmx512m"
```

ou dans le `docker-compose.yml` du devcontainer :

```yaml
services:
# ...
  dev:
#   ...
    environment:
      - GRADLE_OPTS=-Xmx512m
# ...
```

- Limiter la mémoire d'un service Quarkus :
```bash
gradle -Dorg.gradle.jvmargs=-Xmx200m <ma_commande>
```

- Limiter la mémoire du process Node.js pour le front-end :
```bash
NODE_OPTIONS="--max-old-space-size=200" pnpm run --filter apps-store-front start
```

### Gradle.properties

Voici un exemple de configuration `gradle.properties` pour limiter la consommation de mémoire :

```properties
org.gradle.jvmargs=-Xmx512m
```

Cette configuration est nécessaire en plus du réglage des variables d'environnement car les processus Quarkus et les extensions VSCode lancés par Gradle héritent de ces paramètres.

### Paramètres JVM pour VsCode

Pour limiter la consommation de mémoire des extensions Java dans VSCode, vous pouvez ajuster les paramètres JVM dans le fichier `settings.json` de VSCode.

Voici un exemple de configuration pour limiter la mémoire à 512 Mo :

```json
{
    "java.jdt.ls.vmargs": "-Xmx512m"
}
```

### Docker : ajouter du swap sur la VM

Si votre VM Docker manque de mémoire, vous pouvez ajouter du swap pour compenser. Voici comment faire :

Sur l'hôte :

```bash
sudo swapoff /swapfile
sudo rm /swapfile
sudo fallocate -l 4G /swapfile
sudo chmod 600 /swapfile
sudo mkswap /swapfile
sudo swapon /swapfile
```

Cette configuration augmente la taille du swap à 4 Go. Vous pouvez ajuster cette valeur en fonction de vos besoins. Notez cependant que l'utilisation de swap n'est pas aussi performante que la RAM physique ou virtuelle et que la place occupée par le swap se répercute directement sur votre disque dur.

:::warning
Faire un `swapoff` nécessitera un vidage complet du swap, ce qui peut prendre du temps si le swap est très utilisé et si la mémoire disponible est faible. Assurez-vous d'avoir suffisamment de mémoire libre avant d'exécuter cette commande.

Vous pouvez aussi utiliser les commandes de création pour créer un second swapfile le temps d'ajuster les choses sans désactiver le swap existant.
:::

Vérifiez aussi que le swap disponible dans le conteneur correspond bien à vos attentes :

```bash
docker exec -it <id_conteneur> bash
cat /proc/meminfo | grep Swap
```

Vous devriez voir une ligne indiquant la taille totale du swap disponible, par exemple :

```
SwapTotal:       4194300 kB
```

Vérifiez donc que la valeur affichée correspond bien à la taille que vous avez définie précédemment.

### Compresser la RAM pour gagner de l'espace haute performance

Si vous utilisez Linux, vous pouvez activer la compression de la RAM pour gagner de l'espace haute performance. Voici comment faire :

1. Installez le paquet `zram-tools` :

    ```bash
    sudo apt update
    sudo apt install zram-tools
    ```

2. Configurez `zram` en éditant le fichier `/etc/default/zramswap` :

    ```bash
    sudo nano /etc/default/zramswap
    ```

    ```
    ALGO=lz4
    PERCENT=150
    PRIORITY=100
    ```

3. Redémarrez le service `zramswap` :

    ```bash
    sudo systemctl restart zramswap
    ```

4. Utilisez un swap "fallback" sur disque en complément :

    ```bash
    sudo fallocate -l 2G /swapfile
    sudo chmod 600 /swapfile
    sudo mkswap /swapfile
    sudo swapon --priority 10 /swapfile
    ```

    La priorité plus basse (10) permet d'utiliser le swap sur disque uniquement lorsque la RAM compressée est saturé.

### Conclusion

En combinant et ajustant ces paramètres, vous devriez être en mesure de gérer efficacement la consommation de ressources de votre environnement de développement tout en travaillant sur l'application.
