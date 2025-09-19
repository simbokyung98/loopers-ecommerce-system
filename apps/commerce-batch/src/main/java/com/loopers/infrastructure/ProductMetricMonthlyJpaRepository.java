package com.loopers.infrastructure;


import com.loopers.domain.metric.ProductMetricMonthlyModel;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;

public interface ProductMetricMonthlyJpaRepository extends JpaRepository<ProductMetricMonthlyModel, Long> {

    @Modifying
    @Query(value = """
        UPDATE product_metric_monthly mm
        JOIN (
          SELECT product_id, DENSE_RANK() OVER (ORDER BY score DESC) AS rnk
          FROM product_metric_monthly
          WHERE month_start = :monthStart
        ) x ON x.product_id = mm.product_id
        SET mm.rank_value = x.rnk
        WHERE mm.month_start = :monthStart
        """, nativeQuery = true)
    void refreshRanks(@Param("monthStart") LocalDate monthStart);
}
