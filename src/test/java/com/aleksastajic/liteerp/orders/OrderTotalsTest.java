package com.aleksastajic.liteerp.orders;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class OrderTotalsTest {

    @Test
    void lineTotal_isQtyTimesUnitPriceWithScale4() {
        BigDecimal total = OrderTotals.lineTotal(3, new BigDecimal("1.2500"));
        assertEquals("3.7500", total.toPlainString());
    }

    @Test
    void orderTotal_sumsLineTotals() {
        OrderItem a = new OrderItem();
        a.setQty(2);
        a.setUnitPrice(new BigDecimal("10.0000"));

        OrderItem b = new OrderItem();
        b.setQty(1);
        b.setUnitPrice(new BigDecimal("0.5000"));

        BigDecimal total = OrderTotals.orderTotal(List.of(a, b));
        assertEquals("20.5000", total.toPlainString());
    }
}
