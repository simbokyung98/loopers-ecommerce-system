package com.loopers.domain.metric;


import java.time.LocalDate;
import java.util.Optional;

public interface ProductMetricDailyRepository {

    Optional<ProductMetricDailyModel> findByProductIdAndDate(Long productId, LocalDate date);

    void save(ProductMetricDailyModel productMetricDailyModel);


}
