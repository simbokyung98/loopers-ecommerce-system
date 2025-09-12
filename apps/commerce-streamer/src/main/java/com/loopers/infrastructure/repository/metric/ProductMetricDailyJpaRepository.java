package com.loopers.infrastructure.repository.metric;


import com.loopers.domain.metric.ProductMetricDailyModel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Optional;

public interface ProductMetricDailyJpaRepository extends JpaRepository<ProductMetricDailyModel, Long> {

    Optional<ProductMetricDailyModel> findByProductIdAndDate(Long productId, LocalDate date);
}
