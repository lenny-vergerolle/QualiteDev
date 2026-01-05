# TP Order Flow
## Exercice 1: Analyser l'application

### Tâche 1 : Ségrégation des responsabilités 
### 1. Quels sont les principaux domaines métiers de l'application Order flow ?

Les principaux domaines métiers de l'application Order flow sont le panier d'achat et le traitement des commandes


### 2. Comment les services sont-ils conçus pour implémenter les domaines métiers ?

Les services sont conçus en suivant le principe de séparation des responsabilités . Chaque service est responsable d'un domaine métier précis. Par exemple, il peut y avoir un service responsable à la gestion du panier d'achat et un autre service pour le traitement des commandes. Chaque service encapsule la logique métier, les règles de validation et les interactions avec la base de données liées à son domaine.

### 3. Quelles sont les responsabilités des modules :

apps/store-back : Backend de la boutique, gère les API pour commandes, catalogues et stocks.

apps/store-front : Frontend de la boutique, couvre les interfaces utilisateur (IHM) pour clients et commerçants.

libs/kernel : C’est le noyau principal de la bibliothèque, il regroupe les fonctions de base comme la gestion des événements

apps/product-registry-domain-service : Service domaine pour le registre des produits.

apps/product-registry-read-service : Service lecture pour requêtes sur le registre des produits.

libs/bom-platform : Bill of Materials (BOM) pour la plateforme, gère les dépendances communes multi-modules.

libs/cqrs-support : Support pour CQRS, implémente commandes, requêtes, projections et journal d'événements.

libs/sql : Abstractions SQL pour persistance, gère transactions et stockage relationnel.
​


### Tâche 2 : Identifier les concepts principaux
1. Quels sont les concepts principaux utilisés dans l'application Order flow ?

L'application Order flow repose sur plusieurs concepts d'architectures et patterns :

​Event Sourcing : Les états du système sont stockés sous forme de séquence chronologique d'événements immuables dans un event store.

CQRS (Command Query Responsibility Segregation) : Séparation stricte entre les opérations d'écriture (commandes) et de lecture (requêtes), avec des modèles de données distincts


Domain-Driven Design (DDD) : Organisation en bounded contexts avec des agrégats qui constituent les frontières transactionnelles et assurent la cohérence des invariants métier​

Architecture Event-Driven (EDA) : Les événements métiers servent à propager les changements d'état entre les services de manière asynchrone    ​

2. Comment les concepts principaux sont-ils implémentés dans les différents modules ?

libs/cqrs-support : Fournit l'infrastructure pour le pattern CQRS, incluant la gestion des commandes, des requêtes, des projections (vues matérialisées) et du journal d'événements. Ce module abstrait la complexité du CQRS et de l'Event Sourcing.

libs/kernel : Contient les primitives DDD fondamentales comme les agrégats, les entités, les value objects et la gestion du cycle de vie des événements de domaine. Il définit les interfaces de base pour les aggregate roots.

apps/product-registry-domain-service : Implémente le côté écriture (write model) du CQRS pour le registre produits, en gérant les agrégats et en publiant les événements de domaine.

apps/product-registry-read-service : Implémente le côté lecture (read model) en construisant des projections optimisées pour les requêtes à partir des événements publiés.

libs/sql : Fournit l'abstraction pour la persistance relationnelle, notamment pour stocker les événements et les projections avec gestion transactionnelle.

libs/bom-platform : Gère les dépendances partagées (Bill of Materials) pour assurer la cohérence des versions des bibliothèques à travers tous les modules du projet.


Les standards de codage suivent les conventions Quarkus avec camelCase pour méthodes/variables, PascalCase pour classes, et injection de dépendances via CDI.

​
3. Que fait la bibliothèque libs/cqrs-support ? Comment est-elle utilisée ?

libs/cqrs-support implémente le pattern CQRS en fournissant :

Command handlers : Traitent les commandes métier et coordonnent les modifications d'agrégats

Query handlers : Exécutent les requêtes sur les projections optimisées en lecture

Event handlers : Construisent les projections (read models) à partir des événements du journal

Event journal : Stocke l'historique immuable des événements de domaine

Cette bibliothèque est utilisée par les services de domaine (comme product-registry-domain-service) pour gérer les écritures via des commandes qui produisent des événements, et par les services de lecture (comme product-registry-read-service) pour construire des vues matérialisées à partir de ces événements.

​
4. Que fait la bibliothèque libs/bom-platform ?

libs/bom-platform définit un "Bill of Materials" (BOM) qui centralise la déclaration et la gestion des versions des dépendances utilisées par tous les modules de la plateforme. Elle assure la cohérence des versions entre microservices et évite les conflits de dépendances.

​
5. Comment l'implémentation actuelle du CQRS et du Kernel assure-t-elle la fiabilité des états internes ?

La fiabilité est assurée par plusieurs mécanismes :

Chaque agrégat maintient ses invariants métier de manière autonome, garantissant la cohérence forte au sein de ses limites

Les événements immuables dans l'event store constituent l'unique source fiable de l'état du système

L'état actuel d'un agrégat peut être reconstruit à tout moment en rejouant sa séquence d'événements, assurant l'audit et la résilience



### Tâche 3 : Identifier les problèmes de qualité

Pour identifier les problèmes de qualité avec MegaLinter, voici les principaux aspects à analyser après configuration :


Types de problèmes détectables :

Code Java : Violations de conventions Quarkus (nommage, structure de packages), complexité cyclomatique excessive, code dupliqué


Configuration recommandée : Activez les linters Java (PMD, Checkstyle, SpotBugs) tout en désactivant les linters Repository pour éviter les faux positifs liés à la gestion Git. 

