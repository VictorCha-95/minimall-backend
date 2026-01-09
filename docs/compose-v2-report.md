# Compose v2 Verification Report

Scope: docker-compose.local.v2.yml (app + mysql)

## Loop 1

0) Cleanup
- Command: `docker compose -f docker-compose.local.v2.yml down`
- Result: warning about `MYSQL_ROOT_PASSWORD` not set (expected when .env is not loaded)

1) Config validation
- Command: `docker compose -f docker-compose.local.v2.yml --env-file .env.example config`
- Result: success

2) Build + Up
- Command: `docker compose -f docker-compose.local.v2.yml --env-file .env.example up -d --build`
- Result: timeout while pulling base images
- Log: `failed to solve: error from sender: context canceled`

3) Status/logs
- Skipped: compose up did not complete

4) Smoke check
- Skipped: compose up did not complete

5) Gradle tests
- Command: `gradlew.bat test` (rerun with elevated access to Gradle cache)
- Result: success

## Loop 2

0) Cleanup
- Command: `docker compose -f docker-compose.local.v2.yml down`
- Result: warning about `MYSQL_ROOT_PASSWORD` not set (expected when .env is not loaded)

1) Config validation
- Command: `docker compose -f docker-compose.local.v2.yml --env-file .env.example config`
- Result: success

2) Build + Up
- Command: `docker compose -f docker-compose.local.v2.yml --env-file .env.example up -d --build`
- Result: build canceled during context transfer
- Log: `failed to solve: error from sender: context canceled`

3) Status/logs
- Skipped: compose up did not complete

4) Smoke check
- Skipped: compose up did not complete

5) Gradle tests
- Command: `gradlew.bat test` (elevated access)
- Result: success

## Notes / Follow-ups

- `docker compose ... --env-file .env.example config` validates syntax, but `env_file: .env`
  means the container environment still resolves from `.env` at runtime.
- Compose build requires pulling base images; ensure Docker Desktop is running and has
  network access to Docker Hub, or pre-pull the base images.

## Loop 3

0) Cleanup
- Command: `docker compose -f docker-compose.local.v2.yml down`
- Result: warning about `MYSQL_ROOT_PASSWORD` not set (expected when .env is not loaded)

1) Config validation
- Command: `docker compose -f docker-compose.local.v2.yml --env-file .env.example config`
- Result: success

2) Build + Up
- Command: `set DOCKER_BUILDKIT=0&& set COMPOSE_DOCKER_CLI_BUILD=0&& docker compose -f docker-compose.local.v2.yml --env-file .env.example up -d --build`
- Result: timed out while building (compose warned bake requires buildkit)

3) Status/logs
- Command: `docker compose -f docker-compose.local.v2.yml ps`
- Result: no containers running

4) Smoke check
- Skipped: compose up did not complete

5) Gradle tests
- Skipped in this loop (rerun in Loop 5)

## Loop 4

0) Cleanup
- Command: `docker compose -f docker-compose.local.v2.yml down`
- Result: warning about `MYSQL_ROOT_PASSWORD` not set (expected when .env is not loaded)

1) Config validation
- Command: `docker compose -f docker-compose.local.v2.yml --env-file .env.example config`
- Result: success

2) Build + Up
- Pre-pull: `docker pull eclipse-temurin:21-jdk-alpine`, `docker pull eclipse-temurin:21-jre-alpine`
- Command: `docker compose -f docker-compose.local.v2.yml --env-file .env.example up -d --build`
- Result: build canceled during context transfer
- Log: `failed to solve: error from sender: context canceled`

3) Status/logs
- Skipped: compose up did not complete

4) Smoke check
- Skipped: compose up did not complete

5) Gradle tests
- Skipped in this loop (rerun in Loop 5)

## Loop 5

0) Cleanup
- Command: `docker compose -f docker-compose.local.v2.yml down`
- Result: warning about `MYSQL_ROOT_PASSWORD` not set (expected when .env is not loaded)

1) Config validation
- Command: `docker compose -f docker-compose.local.v2.yml --env-file .env.example config`
- Result: success

2) Build + Up
- Command: `set COMPOSE_BAKE=false&& docker compose -f docker-compose.local.v2.yml --env-file .env.example up -d --build`
- Result: build canceled during context transfer
- Log: `failed to solve: error from sender: context canceled`

3) Status/logs
- Skipped: compose up did not complete

4) Smoke check
- Skipped: compose up did not complete

5) Gradle tests
- Command: `gradlew.bat test` (elevated access)
- Result: success

## Loop 6

0) Cleanup
- Command: `docker compose -f docker-compose.local.v2.yml down`
- Result: warning about `MYSQL_ROOT_PASSWORD` not set (expected when .env is not loaded)

1) Config validation
- Command: `docker compose -f docker-compose.local.v2.yml --env-file .env.example config`
- Result: success

2) Build + Up
- Command: `docker compose -f docker-compose.local.v2.yml --env-file .env.example up -d --build`
- Result: build canceled during context transfer
- Log: `failed to solve: error from sender: context canceled`

3) Status/logs
- Skipped: compose up did not complete

4) Smoke check
- Skipped: compose up did not complete

5) Gradle tests
- Command: `gradlew.bat test` (elevated access)
- Result: success

## Loop 7

0) Cleanup
- Command: `docker compose -f docker-compose.local.v2.yml down`
- Result: warning about `MYSQL_ROOT_PASSWORD` not set (expected when .env is not loaded)

1) Config validation
- Command: `docker compose -f docker-compose.local.v2.yml --env-file .env.example config`
- Result: success

2) Build + Up
- Pre-step: `docker builder prune -f` (BuildKit cache cleared)
- Command: `docker compose -f docker-compose.local.v2.yml --env-file .env.example up -d --build`
- Result: build canceled during context transfer
- Log: `failed to solve: error from sender: context canceled`

3) Status/logs
- Skipped: compose up did not complete

4) Smoke check
- Skipped: compose up did not complete

5) Gradle tests
- Command: `gradlew.bat test` (elevated access)
- Result: success

## Loop 8

0) Cleanup
- Command: `docker compose -f docker-compose.local.v2.yml down`
- Result: warning about `MYSQL_ROOT_PASSWORD` not set (expected when .env is not loaded)

1) Config validation
- Command: `docker compose -f docker-compose.local.v2.yml --env-file .env.example config`
- Result: success

2) Build + Up
- Command: `docker compose -f docker-compose.local.v2.yml --env-file .env.example up -d --build` (elevated)
- Result: build canceled during context transfer
- Log: `failed to solve: error from sender: context canceled`

3) Status/logs
- Skipped: compose up did not complete

4) Smoke check
- Skipped: compose up did not complete

5) Gradle tests
- Command: `gradlew.bat test` (elevated access)
- Result: success

## Loop 9 (copied repo: C:\Users\차태승\minimall-build)

0) Cleanup
- Command: `docker compose -f docker-compose.local.v2.yml down`
- Result: success

1) Config validation
- Command: `docker compose -f docker-compose.local.v2.yml --env-file .env.example config`
- Result: success

2) Build + Up
- Command: `docker compose -f docker-compose.local.v2.yml --env-file .env.example up -d --build`
- Result: app/db containers created, but app exited

3) Status/logs
- Command: `docker compose -f docker-compose.local.v2.yml ps -a`
- Result: app/db exited
- Log summary:
  - DB init failed due to SQL syntax error in `db/schema/schema.sql`
  - App failed due to invalid JWT base64 secret

4) Smoke check
- Skipped: app did not stay up

5) Gradle tests
- Skipped in this loop

## Loop 10 (copied repo: C:\Users\차태승\minimall-build)

0) Cleanup
- Command: `docker compose -f docker-compose.local.v2.yml down -v`
- Result: success (volume reset to re-run schema)

1) Config validation
- Command: `docker compose -f docker-compose.local.v2.yml --env-file .env.example config`
- Result: success

2) Build + Up
- Command: `docker compose -f docker-compose.local.v2.yml --env-file .env.example up -d --build`
- Result: success

3) Status/logs
- Command: `docker compose -f docker-compose.local.v2.yml ps`
- Result: app/db running, db healthy

4) Smoke check
- Command: `curl -i http://localhost:8080/actuator/health`
- Result: HTTP 401 with JSON error body (auth required), app confirmed running

5) Gradle tests
- Command: `gradlew.bat test` (elevated access, run in original repo)
- Result: success

## Fixes applied during Loop 10

- Schema fix: add missing comma in `db/schema/schema.sql` to allow MySQL init to complete.
- JWT example secret: set `.env.example` to a valid base64 value (>= 32 bytes) to avoid startup failure.
