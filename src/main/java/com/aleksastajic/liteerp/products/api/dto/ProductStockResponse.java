package com.aleksastajic.liteerp.products.api.dto;

import java.util.UUID;

public record ProductStockResponse(
        UUID productId,
        long stock
) {
}
