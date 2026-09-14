# ADR-002: Use Flyway for versioned database schema changes

- **Status:** Accepted
- **Date:** 2026-09-10

## Context

Patternly's data model will evolve from a small foundational schema into relationship-heavy skills, questions, attempts, and learner-progress data. Ad-hoc database changes would make local environments and later deployments inconsistent.

## Decision

Use Flyway migrations stored in `backend/src/main/resources/db/migration` as the exclusive mechanism for Patternly-owned schema changes. Flyway is configured now, but no migration or domain table is created until the first domain-model decision is complete.

## Consequences

Every schema change will be versioned, reviewable, repeatable, and applied during application startup. Early setup adds a small dependency and startup step, but prevents untracked schema drift.

## Alternatives considered

1. **Hibernate-generated schema** — quick for prototypes, but does not provide an auditable, deterministic schema history.
2. **Manual SQL outside the repository** — simple at first but cannot reliably reproduce environments.
3. **Liquibase** — also capable; Flyway is chosen for its simple SQL-first migration workflow.
