# Lite ERP Backend

## 1) Overview

Lite ERP is a small Spring Boot backend that models products, stock, and orders.
It is built to demonstrate basic data rules, transactions, and integration testing.

## Requirements

- Java 17 (for local run)
- Docker (for integration tests via Testcontainers)
- Docker Compose (optional, for local Postgres and the Docker quickstart)

## 2) Core domain

- Products: create, get, list
- Inventory: stock computed from append-only movements
- Orders: create with stock checks, get, list items (pagination)

Analytics is also included (top products and revenue per day).

## 3) Data model approach

Primary keys are UUIDs.
Money is stored as `NUMERIC(19,4)` and serialized as strings like `"9.9900"`.

### Append-only stock movements

Stock is not stored as a mutable counter.
Instead, the `inventory_movements` table stores deltas (+in, -out).
Current stock is computed by summing movements per product.

This is used to keep an audit trail and avoid silent stock edits.
Updates/deletes are blocked at the DB level for `inventory_movements` (trigger-enforced).

## 4) Quickstart

The database schema is created and validated via Flyway migrations on startup.

### Option A: Docker Compose (preferred)

Build and run API + Postgres:

```bash
docker compose -f docker-compose.app.yml up --build
```

API is on `http://localhost:8080`.
Postgres is exposed on host port `5433` by default.

Verify it is running:

- Swagger UI: `http://localhost:8080/swagger-ui.html`
- Actuator health: `http://localhost:8080/actuator/health`

Stop:

```bash
docker compose -f docker-compose.app.yml down
```

### Option B: Local run (Maven + Postgres)

Start Postgres:

```bash
docker compose up -d postgres
```

Run the API:

```bash
mvn spring-boot:run
```

Verify it is running:

- Swagger UI: `http://localhost:8080/swagger-ui.html`
- Actuator health: `http://localhost:8080/actuator/health`

DB connection defaults (override with env vars if needed):

- `DB_URL` (default: `jdbc:postgresql://localhost:5433/liteerp`)
- `DB_USERNAME` (default: `liteerp`)
- `DB_PASSWORD` (default: `liteerp`)

## 5) Demo (2 minutes)

This repo does not currently expose an HTTP endpoint to create inventory movements.
For a quick demo, seed stock via SQL (from inside the Postgres container).

1) Open Swagger UI:

- `http://localhost:8080/swagger-ui.html`

2) Create a product:

- Call `POST /products` with:

```json
{ "sku": "SKU-DEMO-1", "name": "Demo Product", "price": "9.9900" }
```

Copy the returned `id` as `PRODUCT_ID`.

Tip: set it as a shell variable so substitution is explicit:

```bash
PRODUCT_ID="<paste-uuid-here>"
```

3) Verify stock is initially zero:

- Call `GET /products/{id}/stock`

4) Try to create an order (should fail with conflict):

- Call `POST /orders` with:

```json
{
  "customerRef": "CUST-DEMO",
  "items": [
    { "productId": "PRODUCT_ID", "qty": 1, "unitPrice": "9.9900" }
  ]
}
```

Replace `PRODUCT_ID` with the real UUID.

You should get `409 Conflict` (insufficient stock).

5) Seed stock (+10) via SQL:

```bash
docker compose -f docker-compose.app.yml exec -T postgres psql -U liteerp -d liteerp \
	-c "insert into inventory_movements(product_id, qty, reason) values ('PRODUCT_ID', 10, 'seed');"
```

If you started only Postgres with `docker-compose.yml` (Option B), use:

```bash
docker compose exec -T postgres psql -U liteerp -d liteerp \
	-c "insert into inventory_movements(product_id, qty, reason) values ('PRODUCT_ID', 10, 'seed');"
```

6) Create the order again (should succeed):

- Call `POST /orders` again with the same payload.
- Then call `GET /products/{id}/stock` to see stock decrease.

7) (Optional) Analytics:

Use a date range that includes “today” (UTC) and call:

- `GET /analytics/top-products?from=YYYY-MM-DD&to=YYYY-MM-DD&limit=10`

## 6) OpenAPI / API docs

- Swagger UI: `http://localhost:8080/swagger-ui.html`

If you need the raw OpenAPI JSON, try Springdoc’s default:

- `http://localhost:8080/v3/api-docs`

## 7) Tests

Run all tests:

```bash
mvn test
```

Integration tests use Testcontainers (Postgres).
They are annotated with `@Testcontainers(disabledWithoutDocker = true)`.

Helpful when capturing logs:

```bash
mvn test | tee logs/mvn-test.log
```

## 8) Troubleshooting

### DB connection / ports

- Postgres defaults to host port `5433` (see `docker-compose.yml`).
- If you have Postgres on `5432`, set `POSTGRES_PORT=5432` before `docker compose up`.
- For local runs, override with `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`.

### Flyway migrations

Flyway runs automatically on startup.
If the app fails early, check API logs for migration errors.

### Testcontainers / Docker API version

On newer Docker Engine versions, docker-java may require a higher API version.
If you see an error like “client version … is too old”, run:

```bash
mvn test -Ddocker.api.version=1.44
```

## 9) Roadmap (optional)

- Add an HTTP endpoint for inventory movements (so seeding stock is not SQL-only).
- Add a small, repeatable demo script (curl + DB seed).
- Expand analytics coverage with more scenarios.
