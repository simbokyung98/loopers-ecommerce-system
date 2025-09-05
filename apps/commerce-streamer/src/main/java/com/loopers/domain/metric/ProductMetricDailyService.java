package com.loopers.domain.metric;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class ProductMetricDailyService {

    private final ProductMetricDailyRepository productMetricDailyRepository;


    @Transactional
    public void updateLike(Long productId, int delta) {
        LocalDate today = LocalDate.now();
        ProductMetricDailyModel model = productMetricDailyRepository.findByProductIdAndDate(productId, today)
                .orElseGet(() -> new ProductMetricDailyModel(productId, today));

        model.updateLikeCount(delta);
        productMetricDailyRepository.save(model);
    }

    @Transactional
    public void updateSale(Long productId, int delta) {
        LocalDate today = LocalDate.now();
        ProductMetricDailyModel model = productMetricDailyRepository.findByProductIdAndDate(productId, today)
                .orElseGet(() -> new ProductMetricDailyModel(productId, today));

        model.updateSaleCount(delta);
        productMetricDailyRepository.save(model);
    }

    @Transactional
    public void updateView(Long productId, int delta) {
        LocalDate today = LocalDate.now();
        ProductMetricDailyModel model = productMetricDailyRepository.findByProductIdAndDate(productId, today)
                .orElseGet(() -> new ProductMetricDailyModel(productId, today));

        model.updateViewCount(delta);
        productMetricDailyRepository.save(model);
    }
}
