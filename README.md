# Lite ERP Backend

Lite ERP is a small Spring Boot backend that demonstrates common ERP primitives (products, orders, inventory) with safe data rules and integration tests.

TL;DR
Lite ERP backend showcasing products, orders, append-only inventory movements, and analytics built with Java 17 + Spring Boot and validated with Testcontainers integration tests.

## Highlights for recruiters
- Domain focus: append-only inventory movements with stock aggregation and safe order creation with stock checks.
- Comprehensive integration test coverage: products, stock API, orders, concurrency, pagination, and analytics.
- Production-oriented conventions: Flyway migrations, Docker multi-stage build, and numeric money handling (NUMERIC(19,4)).

## What I learned
- Designing append-only schemas for auditability and easier temporal reasoning.
- Handling financial values precisely (NUMERIC + string serialization) and tests that validate formatting.
- Using Testcontainers to provide reliable integration tests for DB-dependent behavior.

## What’s included

- Products: create, get, list
- Orders: create (with stock checks), get, list items (pagination)
- Inventory: append-only movements + stock aggregation
- Analytics: top products + revenue per day
- OpenAPI UI: swagger UI for exploration
- Problem Details (RFC 7807): consistent error responses

## Tech stack

- Java 17, Spring Boot 3.3.x, Maven
- PostgreSQL 16 + Flyway migrations
- JPA/Hibernate for domain persistence
- Testcontainers (Postgres) for integration tests
- springdoc OpenAPI UI

## Data conventions

- Primary keys are UUIDs.
- Money is stored as `NUMERIC(19,4)` and serialized as strings with 4 decimals (for example `"9.9900"`).
- Inventory movements are append-only (no updates/deletes).

## Prerequisites

- Java 17
- Docker + Docker Compose (required for integration tests)

## Quickstart (local)

1) Start Postgres:

```bash
docker compose up -d postgres
```

By default Postgres is exposed on host port `5433` (container `5432`). Override with `POSTGRES_PORT=5432` if you prefer.

2) Run the API:

```bash
mvn spring-boot:run
```

## Quickstart (Docker)

Run API + Postgres together:

```bash
docker compose -f docker-compose.app.yml up --build
```

## Configuration

The application reads DB configuration from environment variables:

- `DB_URL` (default: `jdbc:postgresql://localhost:5433/liteerp`)
- `DB_USERNAME` (default: `liteerp`)
- `DB_PASSWORD` (default: `liteerp`)

## API

- Health: `GET /actuator/health`
- OpenAPI UI: `GET /swagger-ui.html`

Core routes:

- `POST /products`
- `GET /products`
- `GET /products/{id}`
- `GET /products/{id}/stock`
- `POST /orders`
- `GET /orders/{id}`
- `GET /orders/{id}/items?page=0&size=20`
- `GET /analytics/top-products?from=2026-01-01&to=2026-01-31&limit=10`

## Testing

Recommended pattern for capturing output:

```bash
mvn test | tee logs/mvn-test.log
```

On newer Docker Engine versions (for example Docker Engine 29+), docker-java may require a higher API version. If you see errors like `client version 1.32 is too old`, run:

```bash
mvn test -Ddocker.api.version=1.44
```

## CI

GitHub Actions runs `mvn test` (with Testcontainers) and validates the Docker image build.

## Project structure

See `STRUCTURE.md` for a guided tree and key entrypoints.
