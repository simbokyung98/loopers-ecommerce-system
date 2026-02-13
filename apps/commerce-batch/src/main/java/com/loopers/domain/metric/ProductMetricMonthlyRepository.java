package com.loopers.domain.metric;


import com.loopers.application.dto.MonthlyUpsertRow;

import java.time.LocalDate;
import java.util.List;

public interface ProductMetricMonthlyRepository {

    void save(ProductMetricMonthlyModel productMetricMonthlyModel);

    void bulkUpsert(LocalDate monthStart, LocalDate monthEnd, List<MonthlyUpsertRow> rows);

    void refreshRanks(LocalDate monthStart);

}
