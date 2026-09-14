# Patternly architecture overview

## Purpose

Patternly teaches and assesses problem-solving **patterns**. A data structure is useful context, but it is not the primary learning classification. A question can therefore use multiple skills and multiple data structures.

## Initial runtime architecture

```text
Browser
  |
  v
React frontend
  |
  | HTTPS / JSON REST API
  v
Spring Boot modular monolith
  |
  | JPA / SQL
  v
PostgreSQL
```

The Spring Boot application starts as one deployable application with internally separated modules. This gives us clear domain boundaries without the operational cost, distributed-data concerns, and debugging complexity of microservices.

## Implemented backend foundation

`backend/` is now a Maven-based Spring Boot 4.1.1 application using Java 21 as its project baseline. It exposes only the standard Actuator health endpoint and has no business controllers or domain entities. Its database connection is configured exclusively through environment variables; PostgreSQL is the intended runtime database and Flyway owns future schema changes.

The initial source boundaries are `common`, `learning`, `practice`, `question`, `skill`, and `user`. They are empty Git-tracked directories, not artificial Java classes. This signals ownership boundaries without introducing abstractions before their first real use.

## Component responsibilities

| Component | Responsibility | Why it exists |
| --- | --- | --- |
| React | Learning, practice, recognition, progress, and authentication interfaces | Provides a responsive, component-based web experience independently of backend delivery. |
| Spring Boot | REST APIs, security, domain rules, orchestration, persistence access | Java/Spring is the primary backend and keeps business rules in one cohesive application. |
| PostgreSQL | Durable relational application data | The product is relationship-heavy: skills are hierarchical and questions have many skills and data structures. |
| Redis (later) | Cache, rate limits, short-lived state | It is not a source of truth; add it when measured or functional needs justify it. |
| RabbitMQ (later) | Reliable asynchronous jobs/events | Suitable for AI classification, code evaluation, analytics, and notifications that should not delay a request. |
| Python AI service (later) | Classification, semantic analysis, difficulty/recommendation support | Python is an intelligence helper; Spring Boot remains the product backend and human/admin review remains possible. |
| Docker (later) | Repeatable local and deployment environments | It becomes valuable once executable services and their dependencies exist. |

## Intended backend module boundaries

The future `backend/` application will use packages/modules such as:

```text
auth          identity, authentication, authorization
user          profile and account concerns
skill         skill taxonomy and learner-skill state
question      curated questions and metadata
learning      resources and guided-learning flows
practice      practice-session creation and question selection
assessment    recognition and assessment workflows
submission    implementation submissions and evaluation integration
progress      progress aggregation and mastery signals
analytics     reports and strengths/weaknesses
recommendation adaptive-selection orchestration
```

These are logical boundaries inside one codebase, not network services. Controllers call application services; services enforce domain rules and use repositories; repositories persist to PostgreSQL. Shared cross-cutting concerns—validation, error handling, security, and configuration—remain explicit rather than becoming a generic abstraction layer.

## Conceptual domain model

```text
User --< UserSkill >-- Skill --< QuestionSkill >-- Question --< QuestionDataStructure >-- DataStructure
  |                        ^                         |
  |                        | parent/child            +--< LearningResource (may target Skill or Question)
  +--< Attempt >---------- PracticeSession / AssessmentSession
```

### Core entities

- **User** owns learning state, sessions, and attempts.
- **Skill** represents a pattern or learning unit, has an optional parent skill for navigation/progression, and belongs to a learning area such as DSA or SQL.
- **Question** is curated content with difficulty, explanation, expected complexity, prerequisites, and test/solution metadata.
- **QuestionSkill** is the many-to-many association between questions and skills. It can later hold relationship details such as `primary`, `supporting`, or recognition relevance.
- **DataStructure** is contextual metadata such as Array, HashMap, Graph, or Binary Tree.
- **QuestionDataStructure** is the many-to-many association that preserves a question's multiple contexts without making context its primary classification.
- **UserSkill** records a learner's relationship to a skill: learning state, confidence/mastery, and timestamps. It supports "everything I have learned" selection.
- **PracticeSession** captures a requested skill set, mode, filters, and selected questions so a practice run is reproducible.
- **AssessmentSession** specializes the session idea for recognition assessment, including hidden-pattern presentation and scoring policy.
- **Attempt** records one user's interaction with one question/session. Its eventual result fields keep pattern recognition, justification, implementation correctness, complexity, and optimization separate.
- **LearningResource** represents a linked/embedded resource, notes, examples, or guided content associated with one or more skills (and optionally a question). Licensing and source attribution must be preserved.

The final relational schema and migrations will be designed when persistence begins. This is intentionally a conceptual model, not a premature table contract.

## How future capabilities fit

- **Pattern recognition:** an assessment session hides question skills from the learner, captures selected skills and justification, and compares them to `QuestionSkill` independently of the implementation result.
- **Adaptive practice:** `UserSkill`, attempts, skill relationships, and session outcomes allow question selection based on learned skills, performance, recency, and desired transfer contexts.
- **Mock interviews:** compose timed assessment/practice sessions with evaluator rubrics on the same attempt model instead of duplicating question or skill data.
- **Python AI:** Spring Boot can send an explicit classification/recommendation job to the AI service and store candidate output for admin review; it does not make AI output authoritative by default.
- **Redis:** cache read-heavy skill trees or question selection inputs, rate-limit sensitive endpoints, and hold short-lived state. PostgreSQL remains authoritative.
- **RabbitMQ:** decouple slow/retryable work such as AI classification, code evaluation, and analytics from request/response APIs. A worker consumes the job and reports a result back through persisted state/events.

## Explicitly deferred

No frontend or backend code, database schema, Spring Security setup, Docker configuration, Redis, RabbitMQ, Python service, coding judge, recommendation engine, mock interviews, or production deployment configuration is included in this phase.

## Communication as the system grows

The frontend communicates only with the Spring Boot API over authenticated HTTPS REST endpoints. The backend reads/writes PostgreSQL synchronously. Later, it may use Redis synchronously for non-authoritative cache/state and publish asynchronous work to RabbitMQ. Workers may call the Python service; results return to PostgreSQL for Spring Boot to expose through the API.
