# Backend foundation

## Scope

The `backend/` directory contains a Maven-based Spring Boot 4.1.1 application. It is a single deployable modular monolith, not a group of services. This task adds startup configuration, database-migration readiness, a health endpoint, and one integration test. It does **not** add a product API, entities, repositories, authentication, or tables.

## Prerequisites

- JDK 21 or later. The project compiles with Java 21 language/API compatibility; a newer JDK may run Maven while producing Java 21-compatible output.
- PostgreSQL for normal application startup.
- Internet access on the first Maven Wrapper execution so it can obtain the pinned Maven distribution and dependencies.

No system-wide Maven installation is required: `mvnw.cmd` is the Maven Wrapper committed with the project. Its Windows bootstrap script includes a small compatibility correction so a normal (non-symbolic-link) Maven cache directory can be initialized on a fresh machine.

## Run locally

Create a local PostgreSQL database and a non-production local user. Do not commit its password. In PowerShell, set values only for the current shell:

```powershell
$env:PATTERNLY_DB_URL = "jdbc:postgresql://localhost:5432/patternly"
$env:PATTERNLY_DB_USERNAME = "patternly"
$env:PATTERNLY_DB_PASSWORD = "choose-a-local-password"
$env:SERVER_PORT = "8080" # optional; 8080 is already the default
.\backend\mvnw.cmd -f backend\pom.xml spring-boot:run
```

Then visit or request `http://localhost:8080/actuator/health`. A healthy process returns HTTP 200 and JSON containing `"status":"UP"`.

## Environment variables

| Variable | Required | Purpose |
| --- | --- | --- |
| `PATTERNLY_DB_URL` | Yes | JDBC URL for the PostgreSQL database, including host, port, and database name. |
| `PATTERNLY_DB_USERNAME` | Yes | Database user name. |
| `PATTERNLY_DB_PASSWORD` | Yes | Database password. |
| `SERVER_PORT` | No | HTTP port; defaults to `8080`. |

`application.yml` uses Spring's `${NAME}` placeholder syntax. At startup Spring reads the process environment, substitutes each value into datasource configuration, and then creates the connection pool. The values are absent from source control, so local and production credentials remain separate.

### Application configuration properties

| Property | What it controls | Why it is explicit |
| --- | --- | --- |
| `spring.datasource.*` | JDBC connection URL, credentials, and driver | Makes PostgreSQL an external environment concern rather than committed configuration. |
| `spring.jpa.hibernate.ddl-auto=validate` | Hibernate's schema behavior | It may verify mappings, but cannot create or modify tables behind Flyway's back. |
| `spring.jpa.open-in-view=false` | Whether a persistence session stays open through a web response | Disabling it prevents database access from leaking into future web serialization; it is a production-safe default. |
| `spring.flyway.enabled=true` | Whether Flyway runs at startup | Ensures committed migrations, rather than manual steps, determine the schema. |
| `server.port=${SERVER_PORT:8080}` | Embedded server port | Allows deployment environments to choose a port while keeping local startup simple. |
| `management.endpoints.web.exposure.include=health` | Publicly exposed Actuator web endpoints | Exposes only health, not a broad diagnostics surface. |
| `management.endpoint.health.probes.enabled=true` | Liveness/readiness-style health groups | Prepares the endpoint for future container orchestration without adding Docker now. |

## What happens during startup

1. Java starts `PatternlyBackendApplication.main`.
2. `@SpringBootApplication` asks Spring Boot to configure the application, scan packages below `com.patternly.backend`, and enable auto-configuration based on dependencies.
3. Spring reads `application.yml` and environment variables, then creates PostgreSQL datasource and JPA infrastructure.
4. Flyway scans `db/migration` for versioned SQL files and applies any pending migrations before normal application use. The directory is intentionally empty today.
5. Actuator registers the health contributor; the embedded web server starts on the selected port.
6. An HTTP request to `/actuator/health` receives the aggregated health status.

## Database and Flyway

`spring.jpa.hibernate.ddl-auto=validate` tells Hibernate to validate mapped entities against the database once entities exist; it must not create or alter Patternly tables. `spring.flyway.enabled=true` enables versioned SQL migrations. There are no entities and no migration files yet, therefore no domain tables are created. The first schema task will introduce a numbered SQL migration such as `V1__create_skills.sql` only after the relationships are agreed.

## Test configuration

The automated test activates the `test` profile. `application-test.yml` points at a disposable in-memory H2 database in PostgreSQL compatibility mode. This allows the test to boot the full web application and call the health endpoint without requiring or touching a real PostgreSQL instance. H2 is test-scoped and never used in normal startup.

Run tests and a production build with:

```powershell
.\backend\mvnw.cmd -f backend\pom.xml test
.\backend\mvnw.cmd -f backend\pom.xml verify
```

### Test annotations and framework features

- `@SpringBootApplication` on `PatternlyBackendApplication` combines configuration, component scanning, and dependency-driven auto-configuration. It is the application entry point; a more manual alternative would require separate configuration annotations and explicit bean definitions.
- `@SpringBootTest(webEnvironment = RANDOM_PORT)` starts the whole application, including the embedded web server, on an unused port. This is stronger than a unit test because it proves configuration, database setup, Flyway, Actuator, and HTTP routing work together.
- `@ActiveProfiles("test")` selects `application-test.yml` for this test. It isolates test data sources from normal PostgreSQL configuration.
- `@LocalServerPort` injects the randomly chosen test-server port. The test uses Java's built-in `HttpClient` to call `/actuator/health`, so it confirms a real HTTP response rather than testing a controller method directly.

## Initial package boundaries

| Package | Future responsibility |
| --- | --- |
| `common` | Shared, genuinely cross-cutting concerns such as error handling and validation—only when needed. |
| `skill` | Pattern taxonomy and learner skill state. |
| `question` | Curated questions and their skill/context metadata. |
| `practice` | Session selection and practice flows. |
| `user` | User profile/account concerns. |
| `learning` | Learning resources and guided learning. |

They are boundaries within one Java process. We will add classes only when a concrete task belongs to that boundary.

## Dependency inventory

Spring Boot's parent POM manages compatible versions for all dependencies and plugins listed below. We intentionally do not pin each library version independently, avoiding a set of versions that may not work together.

| Direct dependency | Purpose / used by | Why now | Category |
| --- | --- | --- |
| `spring-boot-starter-webmvc` | Embedded web server and MVC/REST infrastructure; Actuator endpoint delivery | Required to run an HTTP application | Foundational |
| `spring-boot-starter-actuator` | Standard health endpoint | Required for operational verification | Foundational |
| `spring-boot-starter-data-jpa` | JPA/Hibernate and datasource integration | Establishes the persistent-data boundary before entities arrive | Foundational |
| `postgresql` | PostgreSQL JDBC driver at runtime | Connects normal startup to the chosen production-style database | Infrastructure/runtime |
| `spring-boot-starter-flyway` | Flyway startup integration | Ensures schema changes are versioned from the first table | Foundational |
| `flyway-database-postgresql` | PostgreSQL-specific Flyway support | Flyway separates vendor database support in modern versions | Infrastructure/runtime |
| `h2` | In-memory database for the `test` profile | Lets tests run without real credentials or PostgreSQL | Testing-only |
| `spring-boot-starter-webmvc-test` | MVC test support, JUnit, assertions, Spring test utilities | Runs and asserts the health-endpoint integration test | Testing-only |
| `spring-boot-starter-actuator-test` | Actuator test support | Keeps test support aligned with exposed Actuator behavior | Testing-only |
| `spring-boot-starter-data-jpa-test` | JPA/data-source test support | Lets the complete configured application context start in tests | Testing-only |
| `spring-boot-starter-flyway-test` | Flyway test support | Verifies context startup with Flyway enabled | Testing-only |

Important transitive dependencies include the embedded servlet container, Spring Framework, Jackson for JSON health responses, HikariCP for JDBC connection pooling, Hibernate ORM for JPA implementation, Flyway Core, JUnit Jupiter, and AssertJ. The test uses Java 21's built-in `HttpClient`, so no extra HTTP-client test dependency is required. These components arrive through the focused Spring Boot starters; no application code directly selects their versions.

## Verification record

On 2026-09-10, `mvnw.cmd -f backend/pom.xml test` completed successfully: one integration test started the full application with H2, ran Flyway with zero migrations, and received HTTP 200 with `{"status":"UP"}` from `/actuator/health`. `mvnw.cmd -f backend/pom.xml verify` also completed successfully and produced the executable JAR.

A normal-profile manual process requires an available PostgreSQL instance and the three required database environment variables. No PostgreSQL instance was configured in this workspace, so a normal-profile manual start was intentionally not claimed as verified.

## Features deliberately omitted

Redis, RabbitMQ, Spring Security, Python integration, Docker, entities, repositories, migrations, controllers, business APIs, and custom health code are deferred. Adding any now would create surface area without a current feature that needs it.
