# Ticketing System

Support Ticket Management System backend — Java 25, Spring Boot 3.5, PostgreSQL.
See [`specs/001-ticket-management-backend/`](specs/001-ticket-management-backend/) for the
spec, plan, data model, API contract, and task list this was built from, and
[`.specify/memory/constitution.md`](.specify/memory/constitution.md) for the project's
governing engineering rules.

## Prerequisites

- Java 25 (JDK)
- Docker (for local PostgreSQL and for the Testcontainers-backed test suite)

No local Maven install is required — use the committed wrapper (`./mvnw`).

## Run locally

1. Start PostgreSQL:

   ```bash
   docker run --name ticketing-postgres -e POSTGRES_DB=ticketing \
     -e POSTGRES_USER=ticketing -e POSTGRES_PASSWORD=ticketing \
     -p 5432:5432 -d postgres:16
   ```

2. Start the application (Flyway applies the schema automatically on startup):

   ```bash
   ./mvnw spring-boot:run
   ```

3. Confirm it's up:

   ```bash
   curl -s localhost:8080/actuator/health
   ```

4. API docs (Swagger UI): `http://localhost:8080/swagger-ui.html`

See [`specs/001-ticket-management-backend/quickstart.md`](specs/001-ticket-management-backend/quickstart.md)
for a full curl walkthrough of every user story.

### Configuration

Datasource connection is read from environment variables (falling back to the values above for
local dev): `DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USER`, `DB_PASSWORD`. See
`src/main/resources/application.yml`.

## Run the tests

```bash
./mvnw test
```

Requires Docker — the full suite (contract, integration, and the state-machine/concurrency/
restart-durability/search-performance tests) runs against a real PostgreSQL instance via
Testcontainers (constitution Principle III: no in-memory database substitute). Pure unit tests
(`src/test/.../unit/`) need no Docker.

## Project structure

```text
src/main/java/com/frequency/ticketing/
├── web/            # controllers, DTOs, global exception handling
├── domain/         # entities, the ticket state machine, business rules
├── repository/     # Spring Data JPA repositories
└── config/         # cross-cutting config (logging, OpenAPI)

src/main/resources/db/migration/   # Flyway-versioned schema (source of truth — no ddl-auto)

src/test/java/com/frequency/ticketing/
├── contract/       # HTTP-contract tests per endpoint
├── integration/    # full-stack tests (Testcontainers PostgreSQL)
└── unit/           # pure unit tests, no Spring context
```

## Engineering rules

This service follows `.specify/memory/constitution.md` — notably: the ticket status lifecycle
is enforced server-side only (never trust client-supplied status), constructor injection only,
Flyway-only schema changes, and a single consistent JSON error shape for every rejected request.
