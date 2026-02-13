package com.loopers.domain.ranking;


import org.springframework.data.domain.Page;

import java.time.LocalDate;

public interface ProductMetricMonthlyRepository {

    Page<ProductMetricMonthlyModel> findAllByPaging(int page, int size, LocalDate startDate, LocalDate endDate);
}
