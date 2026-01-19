package com.aleksastajic.liteerp.common.money;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class MoneyUtilTest {

    @Test
    void parseAndNormalize_setsScaleTo4() {
        BigDecimal v = MoneyUtil.parseAndNormalize("12.3");
        assertEquals("12.3000", v.toPlainString());
    }

    @Test
    void parseAndNormalize_rejectsRoundingRequired() {
        assertThrows(IllegalArgumentException.class, () -> MoneyUtil.parseAndNormalize("1.23456"));
    }

    @Test
    void format_outputsStringWith4Decimals() {
        assertEquals("10.0000", MoneyUtil.format(new BigDecimal("10")));
    }
}
