package com.aleksastajic.liteerp.products.api.dto;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ProductCreateRequestValidationTest {

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    void rejectsBlankSku() {
        ProductCreateRequest req = new ProductCreateRequest("", "Name", "1.0000");
        assertFalse(validator.validate(req).isEmpty());
    }

    @Test
    void acceptsValidPayload() {
        ProductCreateRequest req = new ProductCreateRequest("SKU-1", "Name", "1.0000");
        assertTrue(validator.validate(req).isEmpty());
    }
}
