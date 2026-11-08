#!/usr/bin/env sh
# À lancer une fois l'application démarrée (docker compose up --build)
BASE_URL="${1:-http://localhost:8080}"
curl -fsS "$BASE_URL/v3/api-docs" -o openapi.json && echo "openapi.json exporté"
