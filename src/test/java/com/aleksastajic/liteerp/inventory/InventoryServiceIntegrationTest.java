package com.aleksastajic.liteerp.inventory;

import com.aleksastajic.liteerp.products.Product;
import com.aleksastajic.liteerp.products.ProductRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Testcontainers(disabledWithoutDocker = true)
@SpringBootTest
class InventoryServiceIntegrationTest {

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
    ProductRepository productRepository;

    @Autowired
    InventoryMovementRepository inventoryMovementRepository;

    @Autowired
    InventoryService inventoryService;

    @Test
    void aggregatesStockFromMovements() {
        Product p = new Product();
        p.setSku("SKU-STOCK-1");
        p.setName("Stock Product");
        p.setPrice(new BigDecimal("1.0000"));
        p = productRepository.save(p);

        InventoryMovement in = new InventoryMovement();
        in.setProductId(p.getId());
        in.setQty(10);
        in.setReason("seed");
        inventoryMovementRepository.save(in);

        InventoryMovement out = new InventoryMovement();
        out.setProductId(p.getId());
        out.setQty(-3);
        out.setReason("consume");
        inventoryMovementRepository.save(out);

        assertEquals(7L, inventoryService.getStock(p.getId()));
    }
}
