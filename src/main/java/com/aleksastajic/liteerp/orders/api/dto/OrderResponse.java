package com.aleksastajic.liteerp.orders.api.dto;

import com.aleksastajic.liteerp.common.money.MoneyUtil;
import com.aleksastajic.liteerp.orders.Order;
import com.aleksastajic.liteerp.orders.OrderTotals;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record OrderResponse(
        UUID id,
        String customerRef,
        String status,
        Instant createdAt,
        String total,
        List<OrderItemResponse> items
) {

    public static OrderResponse from(Order order) {
        List<OrderItemResponse> items = order.getItems().stream().map(OrderItemResponse::from).toList();
        return new OrderResponse(
                order.getId(),
                order.getCustomerRef(),
                order.getStatus().name(),
                order.getCreatedAt(),
                MoneyUtil.format(OrderTotals.orderTotal(order.getItems())),
                items
        );
    }
}
