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

2) Build:

```bash
mvn -q -DskipTests package
```

3) Run:

```bash
mvn spring-boot:run
```

## Endpoints

- Health: `GET /actuator/health`
- OpenAPI UI: `GET /swagger-ui.html`
# lite-erp-backend
