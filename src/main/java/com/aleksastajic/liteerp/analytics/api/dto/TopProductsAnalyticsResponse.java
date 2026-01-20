package com.aleksastajic.liteerp.analytics.api.dto;

import java.time.LocalDate;
import java.util.List;

public record TopProductsAnalyticsResponse(
        LocalDate from,
        LocalDate to,
        int limit,
        List<TopProductRow> topProducts,
        List<RevenuePerDayRow> revenuePerDay
) {
}
