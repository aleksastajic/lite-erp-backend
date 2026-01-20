package com.aleksastajic.liteerp.orders;

import com.aleksastajic.liteerp.inventory.InventoryMovement;
import com.aleksastajic.liteerp.inventory.InventoryMovementRepository;
import com.aleksastajic.liteerp.inventory.InventoryService;
import com.aleksastajic.liteerp.orders.api.dto.OrderCreateItemRequest;
import com.aleksastajic.liteerp.orders.api.dto.OrderCreateRequest;
import com.aleksastajic.liteerp.products.Product;
import com.aleksastajic.liteerp.products.ProductRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.web.client.HttpStatusCodeException;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Testcontainers(disabledWithoutDocker = true)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class OrderConcurrencyIntegrationTest {

    @Container
    @SuppressWarnings("resource")
    static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16")
            .withDatabaseName("liteerp")
            .withUsername("liteerp")
            .withPassword("liteerp");

    @DynamicPropertySource
    static void props(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    TestRestTemplate rest;

    @Autowired
    ProductRepository productRepository;

    @Autowired
    InventoryMovementRepository inventoryMovementRepository;

    @Autowired
    InventoryService inventoryService;

    @Test
    void doesNotOversellUnderConcurrency() throws Exception {
        Product p = new Product();
        p.setSku("SKU-CONC-1");
        p.setName("Concurrency Product");
        p.setPrice(new BigDecimal("1.0000"));
        java.util.UUID productId = productRepository.save(p).getId();

        InventoryMovement seed = new InventoryMovement();
        seed.setProductId(productId);
        seed.setQty(10);
        seed.setReason("seed");
        inventoryMovementRepository.save(seed);

        int threads = 20;
        ExecutorService pool = Executors.newFixedThreadPool(threads);
        try {
            List<Callable<HttpStatus>> tasks = java.util.stream.IntStream.range(0, threads)
                    .mapToObj(i -> (Callable<HttpStatus>) () -> createOrder(productId, "CUST-" + i))
                    .toList();

            int created = 0;
            int conflicts = 0;
            for (var f : pool.invokeAll(tasks)) {
                HttpStatus status;
                try {
                    status = f.get();
                } catch (ExecutionException ex) {
                    throw new RuntimeException(ex.getCause());
                }
                if (status == HttpStatus.CREATED) {
                    created++;
                } else if (status == HttpStatus.CONFLICT) {
                    conflicts++;
                } else {
                    throw new AssertionError("unexpected status: " + status);
                }
            }

            assertEquals(10, created);
            assertEquals(10, conflicts);
            assertEquals(0L, inventoryService.getStock(productId));
        } finally {
            pool.shutdownNow();
        }
    }

    private HttpStatus createOrder(java.util.UUID productId, String customerRef) {
        OrderCreateRequest req = new OrderCreateRequest(
                customerRef,
                List.of(new OrderCreateItemRequest(productId, 1, "1.0000"))
        );

        try {
            ResponseEntity<String> resp = rest.exchange(
                    "/orders",
                    HttpMethod.POST,
                    new HttpEntity<>(req),
                    String.class
            );
            return HttpStatus.valueOf(resp.getStatusCode().value());
        } catch (HttpStatusCodeException ex) {
            return HttpStatus.valueOf(ex.getStatusCode().value());
        }
    }
}
