package com.aleksastajic.liteerp.common.api;

import com.aleksastajic.liteerp.analytics.AnalyticsService;
import com.aleksastajic.liteerp.analytics.api.AnalyticsController;
import com.aleksastajic.liteerp.products.ProductService;
import com.aleksastajic.liteerp.products.api.ProductController;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = {ProductController.class, AnalyticsController.class})
@Import({ApiExceptionHandler.class})
class ApiExceptionHandlerWebMvcTest {

    @Autowired
    MockMvc mvc;

    @MockBean
    ProductService productService;

    @MockBean
    AnalyticsService analyticsService;

    @Test
    void responseStatusExceptionBecomesProblemDetail() throws Exception {
        UUID id = UUID.randomUUID();
        when(productService.get(id)).thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "product not found"));

        mvc.perform(get("/products/{id}", id))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType("application/problem+json"))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.detail").value("product not found"))
                .andExpect(jsonPath("$.instance").value("/products/" + id));
    }

    @Test
    void requestBodyValidationBecomesProblemDetailWithErrors() throws Exception {
        mvc.perform(post("/products")
                        .contentType("application/json")
                        .content("{\"sku\":\"\",\"name\":\"Name\",\"price\":\"1.0000\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType("application/problem+json"))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.detail").value("validation failed"))
                .andExpect(jsonPath("$.errors").isArray());
    }

    @Test
    void queryParamConstraintViolationBecomesProblemDetail() throws Exception {
        mvc.perform(get("/analytics/top-products")
                        .param("from", "2026-01-01")
                        .param("to", "2026-01-02")
                        .param("limit", "0"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType("application/problem+json"))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.detail").value("validation failed"))
                .andExpect(jsonPath("$.errors").isArray());
    }
}
