package com.aleksastajic.liteerp.orders;

import com.aleksastajic.liteerp.orders.api.dto.OrderCreateItemRequest;
import com.aleksastajic.liteerp.orders.api.dto.OrderCreateRequest;
import com.aleksastajic.liteerp.orders.api.dto.OrderResponse;
import com.aleksastajic.liteerp.products.Product;
import com.aleksastajic.liteerp.products.ProductRepository;
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
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@Testcontainers(disabledWithoutDocker = true)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class OrderApiIntegrationTest {

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

    @Test
    void createAndReadOrder() {
        Product p = new Product();
        p.setSku("SKU-ORDER-1");
        p.setName("Order Product");
        p.setPrice(new BigDecimal("9.9900"));
        p = productRepository.save(p);

        OrderCreateRequest create = new OrderCreateRequest(
                "CUST-1",
                List.of(new OrderCreateItemRequest(p.getId(), 2, "9.9900"))
        );

        ResponseEntity<OrderResponse> created = rest.postForEntity("/orders", create, OrderResponse.class);
        assertEquals(HttpStatus.CREATED, created.getStatusCode());
        assertNotNull(created.getBody());
        assertNotNull(created.getBody().id());
        assertEquals("CUST-1", created.getBody().customerRef());
        assertEquals("CREATED", created.getBody().status());
        assertEquals("19.9800", created.getBody().total());
        assertEquals(1, created.getBody().items().size());
        assertEquals("19.9800", created.getBody().items().get(0).lineTotal());

        ResponseEntity<OrderResponse> fetched = rest.getForEntity("/orders/" + created.getBody().id(), OrderResponse.class);
        assertEquals(HttpStatus.OK, fetched.getStatusCode());
        assertNotNull(fetched.getBody());
        assertEquals(created.getBody().id(), fetched.getBody().id());
        assertEquals("19.9800", fetched.getBody().total());
    }
}
