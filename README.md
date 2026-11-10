# API Bibliothèque universitaire — Spring Boot, monolithe modulaire

Projet d'examen M1 Génie Logiciel — ** : Bibliothèque universitaire**.
Java 21 · Spring Boot 3.3 · Spring Modulith · PostgreSQL 16 · Flyway · MapStruct · springdoc-openapi · Docker.

## Lancement en une commande

```bash
docker compose up --build
```
docker compose -f docker-compose.yml -f docker-compose.dev.yml up -d db

L'application démarre après que PostgreSQL est « healthy », puis Flyway applique les migrations.
Les valeurs par défaut conviennent pour une démonstration ; pour les personnaliser : `cp .env.example .env` puis éditer.

| Ressource | URL |
|---|---|
| Swagger UI (un groupe par module) | http://localhost:8080/swagger-ui.html |
| Spécification OpenAPI | http://localhost:8080/v3/api-docs |
| Santé | http://localhost:8080/actuator/health |

## Données de test (migrations de démonstration)

- Adhérents : `1` Alice (ETUDIANT, quota 3) · `2` Bob (ENSEIGNANT, quota 5) · `3` Chloé (ETUDIANT)
- Ouvrages : `1` Clean Code (2 exemplaires : 1, 2) · `2` Domain-Driven Design (exemplaire 3) · `3` Les Misérables (exemplaire 4)
- Collection de requêtes : [`requests/library.http`](requests/library.http) (scénario emprunt → réservation → retour → notifications)

## Modules

```
com.exam
├── catalogue      api/ + internal/   ouvrages, exemplaires, catégories, recherche
├── acteurs        api/ + internal/   adhérents, quotas
├── transactions   api/ + internal/   emprunts, retours, pénalités, réservations  ──(événements)──┐
├── notifications  api/ + internal/   rappels, alertes de retard                  <───────────────┘
└── shared                            erreurs, configuration, pagination
```

`transactions` appelle `catalogue.api` et `acteurs.api` ; `notifications` écoute les événements de `transactions`.
Détails dans [`docs/note-architecture.md`](docs/note-architecture.md).

## Règles de gestion

- Quota : 3 emprunts simultanés (étudiant), 5 (enseignant) — sinon `409`.
- Emprunter un exemplaire déjà emprunté, ou réservé pour un autre adhérent — `409`.
- Durée de prêt 14 jours, pénalité 0,50 € par jour de retard (`library.loan-days`, `library.penalty-per-day`).
- Réserver n'est possible que si aucun exemplaire n'est disponible ; au retour, l'exemplaire est mis de côté pour la
  réservation la plus ancienne (statut `PRETE`), et lui seul peut l'emprunter.
- Chaque emprunt, retour ou réservation prête publie un événement consommé par le module notifications.

## Développement local

```bash
cp .env.example .env
docker compose up -d db                       # PostgreSQL seul
set -a; . ./.env; set +a                      # exporte DB_* dans le shell
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

## Tests

```bash
mvn verify        # nécessite Docker (Testcontainers démarre un PostgreSQL éphémère)
```

- Unitaires : `PenaltyCalculatorTest`, `EmpruntServiceTest` (quotas, retard, pénalité), `ReservationServiceTest` (file d'attente), `TypeAdherentTest`.
- Intégration (un par module) : `CatalogueIntegrationTest`, `ActeursIntegrationTest`, `TransactionsIntegrationTest`, `NotificationsIntegrationTest`.
- Architecture : `ModularityTests` (Spring Modulith `verify()`, génère aussi la documentation PlantUML dans `target/spring-modulith-docs`) et `ArchitectureRulesTest` (ArchUnit).

## Export de la spécification OpenAPI

Application démarrée : `./scripts/export-openapi.sh` → génère `openapi.json` à la racine (à committer avant le rendu).

## Migrations

`src/main/resources/db/migration/<module>/` — 8 migrations versionnées, dont : évolution de table (`V1_3` ajoute l'ISBN),
données de référence (`V1_2` catégories) et données de démonstration (`V1_4`, `V2_2`). Une migration appliquée n'est jamais modifiée.

## Outils d'assistance déclarés
Ce projet a été réalisé avec l'aide de Claude (Anthropic) et ChatGPT pour la structure et de certaines parties du code. La documentation suivante a également été consultée :

Documentation officielle de Spring Boot et Spring Modulith (spring.io)

Documentation de springdoc-openapi (springdoc.org)

Documentation de Flyway / Liquibase

Documentation Docker (Dockerfile multi-étapes, Docker Compose)

ArchUnit (archunit.org), pour qui préfère tester l'architecture sans Spring Modulith

L'auteur a relu, corrigé, adapté et testé l'ensemble du code, réécrit la logique métier et l'interface.
## Bonus couverts

Pipeline CI GitHub Actions (`.github/workflows/ci.yml`) · documentation Spring Modulith générée · Actuator avec healthcheck applicatif dans le compose.
