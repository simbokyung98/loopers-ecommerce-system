package com.loopers.infrastructure;


import com.loopers.domain.metric.ProductMetricDailyModel;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductMetricDailyJpaRepository extends JpaRepository<ProductMetricDailyModel, Long> {

}
