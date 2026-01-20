# Lite ERP (backend)

Backend service for Lite ERP.

## Tech

- Java 17, Spring Boot 3.x, Maven
- PostgreSQL, Flyway, JPA/Hibernate
- Testcontainers (Postgres) for integration tests (added later)
- OpenAPI UI via springdoc
- Structured JSON logs (logback)

## Prerequisites

- Java 17
- Docker + Docker Compose

## Run locally

1) Start Postgres:

```bash
docker compose up -d postgres
```

By default Postgres is exposed on host port `5433` (container `5432`). Override with `POSTGRES_PORT=5432` if you prefer.

2) Build:

```bash
mvn -q -DskipTests package
```

3) Run:

```bash
mvn spring-boot:run
```

## Capturing logs

Recommended pattern:

```bash
mvn test | tee logs/mvn-test.log
```

## Testcontainers integration tests

Integration tests use Testcontainers. If Docker is not available, they will be skipped.

On newer Docker Engine versions (e.g. Docker Engine 29+) the daemon requires Docker API version **>= 1.44**.
If you see errors mentioning an API version like `client version 1.32 is too old` when running `mvn test`, run tests with an explicit API version (this configures the docker-java `api.version` used by Testcontainers):

```bash
mvn test -Ddocker.api.version=1.44
```

## Endpoints

- Health: `GET /actuator/health`
- OpenAPI UI: `GET /swagger-ui.html`
# lite-erp-backend
