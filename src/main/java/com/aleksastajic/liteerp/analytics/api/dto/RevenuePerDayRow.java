package com.aleksastajic.liteerp.analytics.api.dto;

import java.time.LocalDate;

public record RevenuePerDayRow(
        LocalDate day,
        String revenue
) {
}
