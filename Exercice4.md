# Exercice 4 : CQRS et Event Sourcing

## Tâche 1 : Interface Projector

### Rôle du Projector
L'interface Projector transforme les événements en données lisibles. Quand un événement arrive (ex: produit créé), elle met à jour une table de lecture ProductView.

### Rôle du type générique S
S représente le type de table de lecture (ProductView, OrderView, StatsView, etc.).

### Javadoc de la méthode
```java
/**
 * Transforme un événement en table de lecture (S).
 * @param <S> Type de table de lecture
 */
ProjectionResult<S> project(Optional<S> current, EventEnvelope<?> ev);
```

### Avantages de cette approche
- **Interface vs classe** : Permet de changer l'implémentation (PostgreSQL, MongoDB, mémoire) sans modifier le reste du code
- **ProjectionResult** : Conteneur explicite (succès, ignoré, échec) sans exceptions
- **Monade vs exceptions** : Pipeline fluide, type-safe, lisibilité améliorée

---

## Tâche 2 : Outbox Pattern

### Méthodes de OutboxRepository
- `publish()` : Ajouter un événement
- `fetchReady()` : Récupérer les événements prêts
- `delete()` / `markFailed()` : Gérer le cycle de vie

### Garantie de livraison
Une seule transaction sauvegarde les données métier ET l'événement. Même si le service plante après, le polleur relira l'événement.

### Flux concret
```
1. Command → Product.create() → événement
2. Transaction : DB métier + Outbox + EventLog
3. Polleur lit Outbox toutes les secondes
4. Projection → ProductViewRepository.save()
5. Outbox.delete() si OK, sinon retry
```

### Gestion des erreurs
- `attempts` : Nombre d'essais
- `next_attempt_at` : Prochaine tentative
- `last_error` : Raison de l'échec
- Stratégie : Délai exponentiel (30s) jusqu'à max retries, puis blocage 30min

---

## Tâche 3 : Journal d'événements

### Rôle de EventLogRepository
Archive tous les événements de façon immuable. C'est l'historique complet du système.

### Pourquoi seulement append()?
- **Immuabilité** : Pas de modification de l'historique (audit légal)
- **Replay** : Recréer l'état complet d'une entité
- **Source de vérité** : Les événements = backup ultime

### Cas d'usage
- Debug et audit trail
- Replay pour nouveaux services
- Migration de format
- Reconstitution d'état à une date donnée

---

## Tâche 4 : Limites de CQRS

### Défis principaux
| Limite | Description |
|--------|-------------|
| Complexité | 2 bases (write + read) à synchroniser |
| Lag | Le read model peut être décalé |
| Stockage ×2 | Événements + tables de lecture |
| Overhead | Polleur tournant H24 |

### Compensations
- **Dual write** : Outbox garantit transaction unique
- **Ordre garanti** : Polling par `aggregate_version`
- **Idempotence** : Projecteur ignore événements anciens

### Nouvelles limites
- Polleur unique = point de défaillance unique
- Événements infinis → DB pleine (pas de purge)
- Couplage fort : 1 événement → 1 projecteur uniquement

### Projection multiple (1 événement → N tables)
**Problème actuel** : Un seul ProjectionResult par événement

**Solutions**
- Multi-projecteurs : Dispatcher route par type
- Kafka : Projections parallèles asynchrones
- Snapshots : Purger anciens événements périodiquement
- Event bus : Projecteurs s'abonnent sélectivement

### Conclusion
Architecture solide pour démarrer. Évoluer vers **multi-projecteurs + event bus** pour scaler.
