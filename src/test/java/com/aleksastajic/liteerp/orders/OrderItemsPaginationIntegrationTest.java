package com.aleksastajic.liteerp.orders;

import com.aleksastajic.liteerp.inventory.InventoryMovement;
import com.aleksastajic.liteerp.inventory.InventoryMovementRepository;
import com.aleksastajic.liteerp.orders.api.dto.OrderCreateItemRequest;
import com.aleksastajic.liteerp.orders.api.dto.OrderCreateRequest;
import com.aleksastajic.liteerp.orders.api.dto.OrderResponse;
import com.aleksastajic.liteerp.products.Product;
import com.aleksastajic.liteerp.products.ProductRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@Testcontainers(disabledWithoutDocker = true)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class OrderItemsPaginationIntegrationTest {

        private final ObjectMapper objectMapper = new ObjectMapper();

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

    @Test
    void listsOrderItemsWithPagination() {
        Product p = new Product();
        p.setSku("SKU-PAGE-1");
        p.setName("Paging Product");
        p.setPrice(new BigDecimal("1.0000"));
        p = productRepository.save(p);

        InventoryMovement seed = new InventoryMovement();
        seed.setProductId(p.getId());
        seed.setQty(100);
        seed.setReason("seed");
        inventoryMovementRepository.save(seed);

        OrderCreateRequest create = new OrderCreateRequest(
                "CUST-PAGE",
                List.of(
                        new OrderCreateItemRequest(p.getId(), 1, "1.0000"),
                        new OrderCreateItemRequest(p.getId(), 1, "1.0000"),
                        new OrderCreateItemRequest(p.getId(), 1, "1.0000")
                )
        );

        ResponseEntity<OrderResponse> created = rest.postForEntity("/orders", create, OrderResponse.class);
        assertEquals(HttpStatus.CREATED, created.getStatusCode());
        assertNotNull(created.getBody());

        java.util.UUID orderId = created.getBody().id();

        ResponseEntity<String> page0 = rest.exchange(
                "/orders/" + orderId + "/items?page=0&size=2",
                HttpMethod.GET,
                null,
                String.class
        );
        assertEquals(HttpStatus.OK, page0.getStatusCode());

        JsonNode page0Json;
        try {
            page0Json = objectMapper.readTree(page0.getBody());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        assertEquals(3, page0Json.get("totalElements").asInt());
        assertEquals(2, page0Json.get("content").size());

        ResponseEntity<String> page1 = rest.exchange(
                "/orders/" + orderId + "/items?page=1&size=2",
                HttpMethod.GET,
                null,
                String.class
        );
        assertEquals(HttpStatus.OK, page1.getStatusCode());

                JsonNode page1Json;
                try {
                        page1Json = objectMapper.readTree(page1.getBody());
                } catch (Exception e) {
                        throw new RuntimeException(e);
                }
                assertEquals(3, page1Json.get("totalElements").asInt());
                assertEquals(1, page1Json.get("content").size());
    }
}
