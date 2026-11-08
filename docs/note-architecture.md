# Note d'architecture — API Bibliothèque universitaire

## 1. Découpage en modules

Un seul déployable (un JAR), quatre modules métier et un socle technique. Chaque module est un package racine
sous `com.exam` avec un sous-package `api` (contrat public) et un sous-package `internal` (tout le reste).

| Module | Responsabilité | Expose dans `api` |
|---|---|---|
| `catalogue` | ouvrages, exemplaires, catégories, recherche | `CatalogueApi`, `ExemplaireInfo`, `StatutExemplaire` |
| `acteurs` | adhérents, profils et quotas | `AdherentApi`, `AdherentInfo`, `TypeAdherent` |
| `transactions` | emprunts, retours, pénalités, file de réservation | événements `EmpruntEffectue`, `RetourEffectue`, `ReservationDisponible` |
| `notifications` | enregistrement des rappels et alertes | `NotificationApi`, `NotificationDto` |
| `shared` | erreurs, configuration, pagination (aucune logique métier) | module « ouvert » |

Dépendances (sans cycle) : `transactions → catalogue`, `transactions → acteurs` (appels synchrones via les API
publiques) ; `notifications → transactions` (écoute d'événements uniquement). `catalogue` et `acteurs` ne dépendent
d'aucun autre module métier.

## 2. Règles de dépendance et vérification

1. Un module n'importe jamais le `internal` d'un autre ; `api` est déclaré interface nommée Spring Modulith.
2. Aucune relation JPA entre modules : `Emprunt` référence `exemplaireId` et `adherentId`, et aucune clé étrangère
   SQL ne traverse les modules.
3. Les contrôleurs ne manipulent que des DTO (MapStruct) ; jamais d'entité.
4. Vérification automatique : `ModularityTests` (`ApplicationModules.verify()`) et `ArchitectureRulesTest` (ArchUnit :
   relations JPA limitées au module, `api` indépendant de `internal`, contrôleurs sans entités).

## 3. Communication entre modules

- **Synchrone** : `EmpruntService` appelle `AdherentApi` (quota) et `CatalogueApi` (état de l'exemplaire). Le changement
  d'état d'un exemplaire est un `UPDATE` conditionnel atomique : deux emprunts simultanés du même exemplaire ne peuvent
  pas réussir tous les deux (index unique partiel en base en garde-fou).
- **Asynchrone** : à chaque emprunt, retour ou mise de côté d'une réservation, `transactions` publie un événement ;
  `notifications` le consomme avec `@ApplicationModuleListener` (après commit, nouvelle transaction), sans que
  `transactions` connaisse son consommateur.

## 4. Migrations : Flyway plutôt que Liquibase

Flyway a été retenu pour sa simplicité : des fichiers SQL lisibles, relus et expliqués facilement à l'oral, et une
intégration native Spring Boot. Un dossier par module (`db/migration/<module>/`), des versions uniques sur tout le
projet (`V1_x` catalogue, `V2_x` acteurs, `V3_x` transactions, `V4_x` notifications). `ddl-auto=validate` : Hibernate
vérifie le schéma sans jamais le modifier. Une migration appliquée n'est jamais éditée : toute correction est une
nouvelle version (ex. `V1_3` ajoute l'ISBN à une table existante). Le choix de Liquibase aurait apporté des
changesets indépendants du SGBD, utile seulement en multi-base, ce qui n'est pas notre cas.

## 5. Limites connues

- Les événements ne sont pas persistés : si l'application s'arrête entre le commit et la consommation, la notification
  est perdue (amélioration : Event Publication Registry de Spring Modulith).
- Pas d'expiration d'une réservation « prête » : un exemplaire peut rester mis de côté tant que l'adhérent ne vient pas.
- Pas d'authentification (Spring Security / JWT non implémenté).
- Les alertes de retard sont émises au retour du livre ; il n'y a pas de tâche planifiée qui détecte les retards en cours.
- La désactivation d'un adhérent est logique (pas de suppression physique) pour préserver l'historique.
