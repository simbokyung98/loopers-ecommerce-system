package com.loopers.infrastructure;

import com.loopers.application.dto.MonthlyUpsertRow;
import com.loopers.domain.metric.*;
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
public class ProductMetricMonthlyRepositoryImpl implements ProductMetricMonthlyRepository {

    private final ProductMetricMonthlyJpaRepository productMetricMonthlyJpaRepository;
    private final JdbcTemplate jdbc; // 업서트는 JDBC 배치로 처리

    // 기존 save를 꼭 써야 하는 곳이 있다면 유지
    @Override
    public void save(ProductMetricMonthlyModel model) {
        productMetricMonthlyJpaRepository.save(model);
    }

    @Override
    @Transactional
    public void bulkUpsert(LocalDate monthStart, LocalDate monthEnd, List<MonthlyUpsertRow> rows) {
        if (rows == null || rows.isEmpty()) return;

        final String sql = """
            INSERT INTO product_metric_monthly
              (product_id, month_start, month_end, view_count, like_count, order_count, score, rank_value)
            VALUES (?, ?, ?, ?, ?, ?, ?, NULL)
            ON DUPLICATE KEY UPDATE
              month_end   = VALUES(month_end),
              view_count  = VALUES(view_count),
              like_count  = VALUES(like_count),
              order_count = VALUES(order_count),
              score       = VALUES(score),
              rank_value  = NULL
            """;

        jdbc.batchUpdate(sql, new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                MonthlyUpsertRow r = rows.get(i);
                ps.setLong(1, r.productId());
                ps.setObject(2, monthStart);
                ps.setObject(3, monthEnd);
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
    public void refreshRanks(LocalDate monthStart) {
        productMetricMonthlyJpaRepository.refreshRanks(monthStart);
    }
}
