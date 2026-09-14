# Patternly

Patternly is a pattern-centric adaptive learning and interview-preparation platform. It helps learners build, recognize, and apply problem-solving patterns—not simply complete topic-labelled questions.

## Current status

The backend foundation is initialized. It is runnable as a Spring Boot modular monolith, with PostgreSQL/Flyway configuration and a standard health endpoint. Product features and domain tables have not been added.

## Planned shape

```text
React frontend -> Spring Boot modular-monolith API -> PostgreSQL
```

Redis, RabbitMQ, and a Python AI service are intentionally deferred until they solve concrete problems. See [the architecture overview](docs/architecture/overview.md) and [ADR-001](docs/decisions/ADR-001-modular-monolith.md).

## Repository layout

- `backend/` — future Spring Boot application
- `frontend/` — future React application
- `ai-service/` — future Python intelligence service, introduced only when needed
- `infrastructure/` — future Docker/deployment configuration
- `docs/` — architecture, decisions, development guidance, and progress records

## Run the backend

See [backend setup](docs/development/backend-foundation.md) for required environment variables and commands. Once PostgreSQL is available locally:

```powershell
$env:PATTERNLY_DB_URL = "jdbc:postgresql://localhost:5432/patternly"
$env:PATTERNLY_DB_USERNAME = "patternly"
$env:PATTERNLY_DB_PASSWORD = "choose-a-local-password"
.\backend\mvnw.cmd -f backend\pom.xml spring-boot:run
```

Then request `http://localhost:8080/actuator/health`.
