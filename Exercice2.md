# Tâche 5 - Réponses (étudiant)

## Question 1 : Tests unitaires vs intégration

**Tests unitaires** : on teste UNE classe/méthode en mockant tout (repos, services). L'idée c'est d'être rapide et isolé.
- Ex: `Product.updateName()` sans toucher la DB.

**Tests intégration** : on teste vraiment les interactions (DB réelle avec TestContainers, API externes, etc.). Plus lent mais plus réaliste.

## Question 2 : Couverture 100% ?

Non, c'est useless voire nuisible.

**Pourquoi ?**
- 80% du code c'est du boilerplate (getters, mappers) → pas de valeur business
- Focus sur chemins critiques : guards métier (`canUpdateDetails()`), business logic
- Mutation testing >> coverage % (tue les mutants, pas les lignes)

## Question 3 : Avantages Onion pour les tests

L'architecture oignon = domain au centre, infra à la périphérie → test parfait !

```
Test Product (Tâche 3) :
Product.updateName() : pur domain → AUCUN mock/DB
ProductViewProjector : switch events → pur algo 
ReadProductService : mock broadcaster/repo
JpaRepository : H2/TestContainers
```

Chaque couche testée indépendamment → rapide + fiable !

## Question 4 : Nomenclature packages

```
kernel/         ←  Domain PURE (Product, SkuId, Value Objects)
application/    ←  Use cases métier (ReadProductService)
infra/          ←  Technique pluggable
    ├─ jpa/       ←  Persistance (JPA entities/repos)
    ├─ api/       ←  REST read-side (ProductRegistryQueryResource)  
    ├─ web/       ←  DTOs + controllers
    └─ client/    ←  @RestClient (ProductRegistryService)
```

