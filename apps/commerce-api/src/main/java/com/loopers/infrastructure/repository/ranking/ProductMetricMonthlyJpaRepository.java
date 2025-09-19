package com.loopers.infrastructure.repository.ranking;


import com.loopers.domain.ranking.ProductMetricMonthlyModel;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductMetricMonthlyJpaRepository extends JpaRepository<ProductMetricMonthlyModel, Long> {

}
