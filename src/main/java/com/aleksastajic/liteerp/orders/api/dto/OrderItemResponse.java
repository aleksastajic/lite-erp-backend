package com.aleksastajic.liteerp.orders.api.dto;

import com.aleksastajic.liteerp.common.money.MoneyUtil;
import com.aleksastajic.liteerp.orders.OrderItem;
import com.aleksastajic.liteerp.orders.OrderTotals;

import java.util.UUID;

public record OrderItemResponse(
        UUID id,
        UUID productId,
        int qty,
        String unitPrice,
        String lineTotal
) {

    public static OrderItemResponse from(OrderItem item) {
        return new OrderItemResponse(
                item.getId(),
                item.getProductId(),
                item.getQty(),
                MoneyUtil.format(item.getUnitPrice()),
                MoneyUtil.format(OrderTotals.lineTotal(item.getQty(), item.getUnitPrice()))
        );
    }
}
