package com.aleksastajic.liteerp.common.money;

import java.math.BigDecimal;
import java.math.RoundingMode;

public final class MoneyUtil {

    private MoneyUtil() {
    }

    public static BigDecimal parseAndNormalize(String amount) {
        if (amount == null) {
            throw new IllegalArgumentException("amount is required");
        }
        try {
            return new BigDecimal(amount).setScale(4, RoundingMode.UNNECESSARY);
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("amount must be a valid decimal string");
        } catch (ArithmeticException ex) {
            throw new IllegalArgumentException("amount must have scale <= 4");
        }
    }

    public static String format(BigDecimal amount) {
        if (amount == null) {
            return null;
        }
        return amount.setScale(4, RoundingMode.UNNECESSARY).toPlainString();
    }
}
