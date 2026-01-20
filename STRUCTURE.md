# Project structure

This repository is a single Spring Boot service.

## Top-level

- `pom.xml` — Maven build
- `docker-compose.yml` — Postgres for local development
- `docker-compose.app.yml` — API + Postgres (Docker build + run)
- `Dockerfile` — multi-stage image build
- `.github/workflows/ci.yml` — CI workflow
- `logs/` — local log output (kept via `logs/.gitkeep`)

## Source code

- `src/main/java/com/aleksastajic/liteerp/LiteErpApplication.java` — Spring Boot entrypoint

### Packages

- `com.aleksastajic.liteerp.products`
  - Domain: `Product`
  - Persistence: `ProductRepository`
  - Service: `ProductService`
  - API: `products/api/ProductController`

- `com.aleksastajic.liteerp.orders`
  - Domain: `Order`, `OrderItem`, `OrderStatus`
  - Persistence: `OrderRepository`, `OrderItemRepository`
  - Service: `OrderService`
  - API: `orders/api/OrderController`
  - DTOs: `orders/api/dto/*`

- `com.aleksastajic.liteerp.inventory`
  - Domain: `InventoryMovement` (append-only)
  - Persistence: `InventoryMovementRepository`
  - Service: `InventoryService` (stock aggregation)

- `com.aleksastajic.liteerp.analytics`
  - Service: `AnalyticsService` (SQL aggregation)
  - API: `analytics/api/AnalyticsController`
  - DTOs: `analytics/api/dto/*`

- `com.aleksastajic.liteerp.common`
  - API: `common/api/ApiExceptionHandler` (Problem Details) and `common/api/OpenApiConfig`
  - Money: `common/money/MoneyUtil`

## Resources

- `src/main/resources/application.yml` — default configuration (reads `DB_*` env vars)
- `src/main/resources/db/migration/*` — Flyway migrations

## Tests

- Unit tests: `src/test/java/**` (pure Java tests)
- Integration tests: `@SpringBootTest` + Testcontainers (Postgres)

Key integration tests:

- `products/ProductApiIntegrationTest`
- `products/ProductStockIntegrationTest`
- `orders/OrderApiIntegrationTest`
- `orders/OrderConcurrencyIntegrationTest`
- `orders/OrderItemsPaginationIntegrationTest`
- `inventory/InventoryServiceIntegrationTest`
- `analytics/AnalyticsTopProductsIntegrationTest`
