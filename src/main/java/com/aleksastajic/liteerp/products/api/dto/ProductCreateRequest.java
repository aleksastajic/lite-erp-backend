package com.aleksastajic.liteerp.products.api.dto;

import jakarta.validation.constraints.NotBlank;

public record ProductCreateRequest(
        @NotBlank String sku,
        @NotBlank String name,
        @NotBlank String price
) {
}
