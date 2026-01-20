package com.aleksastajic.liteerp.analytics.api.dto;

import java.util.UUID;

public record TopProductRow(
        UUID productId,
        String sku,
        String name,
        long qtySold,
        String revenue
) {
}
