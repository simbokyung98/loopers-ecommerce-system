package com.loopers.infrastructure.metric;


import com.loopers.domain.metric.ProductMetricDailyModel;
import com.loopers.domain.metric.ProductMetricDailyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

@RequiredArgsConstructor
@Repository
public class ProductMetricDailyRepositoryImpl implements ProductMetricDailyRepository {

    private final ProductMetricDailyJpaRepository productMetricDailyJpaRepository;


    @Override
    public Optional<ProductMetricDailyModel> findByProductIdAndDate(Long productId, LocalDate date) {
        return productMetricDailyJpaRepository.findByProductIdAndDate(productId, date);
    }

    @Override
    public void save(ProductMetricDailyModel productMetricDailyModel) {
        productMetricDailyJpaRepository.save(productMetricDailyModel);
    }
}
