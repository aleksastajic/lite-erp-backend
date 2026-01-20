package com.aleksastajic.liteerp.analytics;

import com.aleksastajic.liteerp.products.Product;
import com.aleksastajic.liteerp.products.ProductRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@Testcontainers(disabledWithoutDocker = true)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class AnalyticsTopProductsIntegrationTest {

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
    JdbcTemplate jdbc;

    @Autowired
    ProductRepository productRepository;

    @Test
    void topProductsAndRevenuePerDay() throws Exception {
        Product p1 = new Product();
        p1.setSku("SKU-ANA-1");
        p1.setName("Analytics Product 1");
        p1.setPrice(new BigDecimal("5.0000"));
        p1 = productRepository.save(p1);

        Product p2 = new Product();
        p2.setSku("SKU-ANA-2");
        p2.setName("Analytics Product 2");
        p2.setPrice(new BigDecimal("2.0000"));
        p2 = productRepository.save(p2);

        // 2026-01-01: p1 qty=2 @ 5.0000 => 10.0000
        UUID o1 = UUID.randomUUID();
        jdbc.update(
                "INSERT INTO orders(id, customer_ref, status, created_at) VALUES (?,?,?,?)",
                o1,
                "CUST-A",
                "CREATED",
                Timestamp.from(Instant.parse("2026-01-01T10:00:00Z"))
        );
        jdbc.update(
                "INSERT INTO order_items(id, order_id, product_id, qty, unit_price) VALUES (?,?,?,?,?)",
                UUID.randomUUID(),
                o1,
                p1.getId(),
                2,
                new BigDecimal("5.0000")
        );

        // 2026-01-02: p1 qty=1 @ 5.0000 => 5.0000; p2 qty=4 @ 2.0000 => 8.0000
        UUID o2 = UUID.randomUUID();
        jdbc.update(
                "INSERT INTO orders(id, customer_ref, status, created_at) VALUES (?,?,?,?)",
                o2,
                "CUST-B",
                "CREATED",
                Timestamp.from(Instant.parse("2026-01-02T12:00:00Z"))
        );
        jdbc.update(
                "INSERT INTO order_items(id, order_id, product_id, qty, unit_price) VALUES (?,?,?,?,?)",
                UUID.randomUUID(),
                o2,
                p1.getId(),
                1,
                new BigDecimal("5.0000")
        );
        jdbc.update(
                "INSERT INTO order_items(id, order_id, product_id, qty, unit_price) VALUES (?,?,?,?,?)",
                UUID.randomUUID(),
                o2,
                p2.getId(),
                4,
                new BigDecimal("2.0000")
        );

        ResponseEntity<String> resp = rest.getForEntity(
                "/analytics/top-products?from=2026-01-01&to=2026-01-02&limit=10",
                String.class
        );
        assertEquals(HttpStatus.OK, resp.getStatusCode());
        assertNotNull(resp.getBody());

        JsonNode json = objectMapper.readTree(resp.getBody());
        assertEquals("2026-01-01", json.get("from").asText());
        assertEquals("2026-01-02", json.get("to").asText());

        JsonNode top = json.get("topProducts");
        assertEquals(2, top.size());

        // p2 sold 4 (revenue 8.0000) should be top by qty
        assertEquals(p2.getId().toString(), top.get(0).get("productId").asText());
        assertEquals(4, top.get(0).get("qtySold").asInt());
        assertEquals("8.0000", top.get(0).get("revenue").asText());

        // p1 sold 3 total (revenue 15.0000)
        assertEquals(p1.getId().toString(), top.get(1).get("productId").asText());
        assertEquals(3, top.get(1).get("qtySold").asInt());
        assertEquals("15.0000", top.get(1).get("revenue").asText());

        JsonNode perDay = json.get("revenuePerDay");
        assertEquals(2, perDay.size());
        assertEquals("2026-01-01", perDay.get(0).get("day").asText());
        assertEquals("10.0000", perDay.get(0).get("revenue").asText());
        assertEquals("2026-01-02", perDay.get(1).get("day").asText());
                assertEquals("13.0000", perDay.get(1).get("revenue").asText());
    }
}
