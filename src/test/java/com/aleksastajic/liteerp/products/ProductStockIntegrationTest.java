package com.aleksastajic.liteerp.products;

import com.aleksastajic.liteerp.inventory.InventoryMovement;
import com.aleksastajic.liteerp.inventory.InventoryMovementRepository;
import com.aleksastajic.liteerp.products.api.dto.ProductStockResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@Testcontainers(disabledWithoutDocker = true)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ProductStockIntegrationTest {

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
    void returnsCurrentStock() {
        Product p = new Product();
        p.setSku("SKU-STOCK-API-1");
        p.setName("Stock API Product");
        p.setPrice(new BigDecimal("2.0000"));
        p = productRepository.save(p);

        InventoryMovement m = new InventoryMovement();
        m.setProductId(p.getId());
        m.setQty(5);
        m.setReason("seed");
        inventoryMovementRepository.save(m);

        ResponseEntity<ProductStockResponse> resp = rest.getForEntity("/products/" + p.getId() + "/stock", ProductStockResponse.class);
        assertEquals(HttpStatus.OK, resp.getStatusCode());
        assertNotNull(resp.getBody());
        assertEquals(p.getId(), resp.getBody().productId());
        assertEquals(5L, resp.getBody().stock());
    }
}
