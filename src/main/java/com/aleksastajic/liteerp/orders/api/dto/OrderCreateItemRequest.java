package com.aleksastajic.liteerp.orders.api.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record OrderCreateItemRequest(
        @NotNull UUID productId,
        @Min(1) int qty,
        @NotBlank String unitPrice
) {
}
