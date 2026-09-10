# Patternly

Patternly is a pattern-centric adaptive learning and interview-preparation platform. It helps learners build, recognize, and apply problem-solving patterns—not simply complete topic-labelled questions.

## Current status

This repository is in its architecture-initialization phase. No application features, dependencies, or runtime services have been added yet.

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

## Next step

Initialize the Spring Boot backend as a modular monolith and establish its first domain modules, configuration, tests, and database-migration baseline.
