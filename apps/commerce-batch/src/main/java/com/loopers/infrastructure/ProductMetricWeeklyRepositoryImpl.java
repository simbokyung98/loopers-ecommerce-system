package com.loopers.infrastructure;


import com.loopers.domain.metric.ProductMetricWeeklyModel;
import com.loopers.domain.metric.ProductMetricWeeklyRepository;
import com.loopers.application.dto.WeeklyUpsertRow;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

@RequiredArgsConstructor
@Repository
public class ProductMetricWeeklyRepositoryImpl implements ProductMetricWeeklyRepository {

    private final ProductMetricWeeklyJpaRepository productMetricWeeklyJpaRepository;
    private final JdbcTemplate jdbc;

    @Override
    public void save(ProductMetricWeeklyModel productMetricWeeklyModel) {
        productMetricWeeklyJpaRepository.save(productMetricWeeklyModel);
    }

    @Override
    @Transactional
    public void bulkUpsert(LocalDate weekStart, LocalDate weekEnd, List<WeeklyUpsertRow> rows) {
        if (rows == null || rows.isEmpty()) return;

        final String sql = """
            INSERT INTO product_metric_weekly
              (product_id, week_start, week_end, view_count, like_count, order_count, score, rank_value)
            VALUES (?, ?, ?, ?, ?, ?, ?, NULL)
            ON DUPLICATE KEY UPDATE
              week_end   = VALUES(week_end),
              view_count = VALUES(view_count),
              like_count = VALUES(like_count),
              order_count= VALUES(order_count),
              score      = VALUES(score),
              rank_value = NULL
            """;

        jdbc.batchUpdate(sql, new BatchPreparedStatementSetter() {
            @Override public void setValues(PreparedStatement ps, int i) throws SQLException {
                WeeklyUpsertRow r = rows.get(i);
                ps.setLong(1, r.productId());
                ps.setObject(2, weekStart); // JDBC 4.2: LocalDate OK
                ps.setObject(3, weekEnd);
                ps.setLong(4, r.viewSum());
                ps.setLong(5, r.likeSum());
                ps.setLong(6, r.orderSum());
                ps.setLong(7, r.score());
            }
            @Override public int getBatchSize() { return rows.size(); }
        });
    }

    @Override
    @Transactional
    public void refreshRanks(LocalDate weekStart) {
        productMetricWeeklyJpaRepository.refreshRanks(weekStart);
    }
}
