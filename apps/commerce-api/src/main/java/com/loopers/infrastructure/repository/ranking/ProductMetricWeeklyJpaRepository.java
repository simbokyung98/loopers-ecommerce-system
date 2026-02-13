package com.loopers.infrastructure.repository.ranking;

import com.loopers.domain.ranking.ProductMetricWeeklyModel;
import org.springframework.data.jpa.repository.JpaRepository;


public interface ProductMetricWeeklyJpaRepository extends JpaRepository<ProductMetricWeeklyModel, Long> {


}
