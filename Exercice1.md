# TP Order Flow
## Exercice 1: Analyser l'application

### 1. Quels sont les principaux domaines métiers de l'application Order flow ?

Les principaux domaines métiers de l'application Order flow sont l panier d'achat et le traitement des commandes


### 2. Comment les services sont-ils conçus pour implémenter les domaines métiers ?

Les services sont conçus en suivant le principe de séparation des responsabilités, où chaque service est responsable d'un domaine métier spécifique. Par exemple, il peut y avoir un service dédié à la gestion du panier d'achat et un autre service pour le traitement des commandes. Chaque service encapsule la logique métier, les règles de validation et les interactions avec la base de données liées à son domaine.

### 3. Quelles sont les responsabilités des modules :

apps/store-back : Backend de la boutique, gère les API pour commandes, catalogues et stocks.

apps/store-front : Frontend de la boutique, couvre les interfaces utilisateur (IHM) pour clients et commerçants.

libs/kernel : Noyau central, fournit les primitives communes comme gestion d'événements et agrégats.

apps/product-registry-domain-service : Service domaine pour le registre des produits (écritures/agrégats).

apps/product-registry-read-service : Service lecture pour requêtes sur le registre des produits (vues/projections).

libs/bom-platform : Bill of Materials (BOM) pour la plateforme, gère les dépendances communes multi-modules.

libs/cqrs-support : Support pour CQRS, implémente commandes, requêtes, projections et journal d'événements.

libs/sql : Abstractions SQL pour persistance, gère transactions et stockage relationnel.
​