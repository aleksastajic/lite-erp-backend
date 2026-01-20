package com.aleksastajic.liteerp.analytics.api;

import com.aleksastajic.liteerp.analytics.AnalyticsService;
import com.aleksastajic.liteerp.analytics.api.dto.TopProductsAnalyticsResponse;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@Validated
@RestController
@RequestMapping("/analytics")
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    public AnalyticsController(AnalyticsService analyticsService) {
        this.analyticsService = analyticsService;
    }

    @GetMapping("/top-products")
    public TopProductsAnalyticsResponse topProducts(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(defaultValue = "10") @Min(1) @Max(100) int limit
    ) {
        return new TopProductsAnalyticsResponse(
                from,
                to,
                limit,
                analyticsService.topProducts(from, to, limit),
                analyticsService.revenuePerDay(from, to)
        );
    }
}
