package com.aleksastajic.liteerp.orders;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

public final class OrderTotals {

    private OrderTotals() {
    }

    public static BigDecimal lineTotal(int qty, BigDecimal unitPrice) {
        if (qty <= 0) {
            throw new IllegalArgumentException("qty must be > 0");
        }
        if (unitPrice == null) {
            throw new IllegalArgumentException("unitPrice is required");
        }
        return unitPrice.multiply(BigDecimal.valueOf(qty)).setScale(4, RoundingMode.UNNECESSARY);
    }

    public static BigDecimal orderTotal(List<OrderItem> items) {
        BigDecimal total = BigDecimal.ZERO.setScale(4, RoundingMode.UNNECESSARY);
        for (OrderItem item : items) {
            total = total.add(lineTotal(item.getQty(), item.getUnitPrice()));
        }
        return total;
    }
}
