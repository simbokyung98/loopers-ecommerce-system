package com.loopers.infrastructure;

import com.loopers.domain.metric.ProductMetricWeeklyModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;


public interface ProductMetricWeeklyJpaRepository extends JpaRepository<ProductMetricWeeklyModel, Long> {

    @Modifying
    @Query(value = """
        UPDATE product_metric_weekly wm
        JOIN (
          SELECT product_id, DENSE_RANK() OVER (ORDER BY score DESC) AS rnk
          FROM product_metric_weekly
          WHERE week_start = :weekStart
        ) x ON x.product_id = wm.product_id
        SET wm.rank_value = x.rnk
        WHERE wm.week_start = :weekStart
        """, nativeQuery = true)
    void refreshRanks(@Param("weekStart") LocalDate weekStart);

}
