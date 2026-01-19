package com.aleksastajic.liteerp.products.api.dto;

import com.aleksastajic.liteerp.common.money.MoneyUtil;
import com.aleksastajic.liteerp.products.Product;

import java.time.Instant;
import java.util.UUID;

public record ProductResponse(
        UUID id,
        String sku,
        String name,
        String price,
        Instant createdAt
) {

    public static ProductResponse from(Product product) {
        return new ProductResponse(
                product.getId(),
                product.getSku(),
                product.getName(),
                MoneyUtil.format(product.getPrice()),
                product.getCreatedAt()
        );
    }
}
