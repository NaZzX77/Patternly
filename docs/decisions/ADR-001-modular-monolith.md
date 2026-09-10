# ADR-001: Start Patternly as a modular monolith

- **Status:** Accepted
- **Date:** 2026-09-10

## Context

Patternly needs several cohesive product domains—skills, questions, learning, practice, assessments, and progress. It may later include asynchronous work and an AI helper, but the first product does not need independently deployable services.

## Decision

Build the initial backend as one Spring Boot application with explicit domain-oriented package boundaries and PostgreSQL persistence. Keep React as a separate client application. Introduce Redis, RabbitMQ, workers, and a Python AI service only when a concrete feature requires them.

## Consequences

This minimizes deployment and local-development complexity, enables straightforward transactional consistency, and makes early debugging easier. It requires discipline to keep package boundaries clear, but avoids premature distributed-system concerns.

## Alternatives considered

1. **Microservices immediately** — gives independent deployment but adds network failure modes, distributed data ownership, observability, and development overhead before the product needs them.
2. **Monolithic frontend and backend** — simpler initial setup, but a separate React client better matches the intended stack and encourages a stable API boundary.
3. **Python as the primary backend** — would weaken the stated Java/Spring learning goal and make AI concerns central too early.
