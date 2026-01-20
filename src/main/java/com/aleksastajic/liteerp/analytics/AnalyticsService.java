package com.aleksastajic.liteerp.analytics;

import com.aleksastajic.liteerp.analytics.api.dto.RevenuePerDayRow;
import com.aleksastajic.liteerp.analytics.api.dto.TopProductRow;
import com.aleksastajic.liteerp.common.money.MoneyUtil;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;

import java.sql.Types;

import static org.springframework.http.HttpStatus.BAD_REQUEST;

@Service
public class AnalyticsService {

    private final NamedParameterJdbcTemplate jdbc;

    public AnalyticsService(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public List<TopProductRow> topProducts(LocalDate from, LocalDate to, int limit) {
        DateRange range = DateRange.of(from, to);
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("from", range.fromInclusive(), Types.TIMESTAMP_WITH_TIMEZONE)
                .addValue("to", range.toExclusive(), Types.TIMESTAMP_WITH_TIMEZONE)
                .addValue("limit", limit);

        String sql = """
                SELECT
                  oi.product_id AS product_id,
                  p.sku AS sku,
                  p.name AS name,
                  SUM(oi.qty) AS qty_sold,
                  SUM(oi.qty * oi.unit_price) AS revenue
                FROM orders o
                JOIN order_items oi ON oi.order_id = o.id
                JOIN products p ON p.id = oi.product_id
                WHERE o.created_at >= :from AND o.created_at < :to
                GROUP BY oi.product_id, p.sku, p.name
                ORDER BY qty_sold DESC, revenue DESC
                LIMIT :limit
                """;

        return jdbc.query(sql, params, (rs, rowNum) -> new TopProductRow(
                UUID.fromString(rs.getString("product_id")),
                rs.getString("sku"),
                rs.getString("name"),
                rs.getLong("qty_sold"),
                MoneyUtil.format(rs.getBigDecimal("revenue"))
        ));
    }

    public List<RevenuePerDayRow> revenuePerDay(LocalDate from, LocalDate to) {
        DateRange range = DateRange.of(from, to);
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("from", range.fromInclusive(), Types.TIMESTAMP_WITH_TIMEZONE)
                .addValue("to", range.toExclusive(), Types.TIMESTAMP_WITH_TIMEZONE);

        String sql = """
                SELECT
                  ((o.created_at AT TIME ZONE 'UTC')::date) AS day,
                  SUM(oi.qty * oi.unit_price) AS revenue
                FROM orders o
                JOIN order_items oi ON oi.order_id = o.id
                WHERE o.created_at >= :from AND o.created_at < :to
                GROUP BY day
                ORDER BY day
                """;

        return jdbc.query(sql, params, (rs, rowNum) -> {
            LocalDate day = rs.getObject("day", LocalDate.class);
            BigDecimal revenue = rs.getBigDecimal("revenue");
            return new RevenuePerDayRow(day, MoneyUtil.format(revenue));
        });
    }

    private record DateRange(OffsetDateTime fromInclusive, OffsetDateTime toExclusive) {

        static DateRange of(LocalDate from, LocalDate to) {
            if (from == null || to == null) {
                throw new ResponseStatusException(BAD_REQUEST, "from and to are required");
            }
            if (to.isBefore(from)) {
                throw new ResponseStatusException(BAD_REQUEST, "to must be >= from");
            }
            OffsetDateTime fromInclusive = from.atStartOfDay(ZoneOffset.UTC).toOffsetDateTime();
            OffsetDateTime toExclusive = to.plusDays(1).atStartOfDay(ZoneOffset.UTC).toOffsetDateTime();
            return new DateRange(fromInclusive, toExclusive);
        }
    }
}
