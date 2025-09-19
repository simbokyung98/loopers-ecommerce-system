package com.loopers.domain.metric;

import com.loopers.application.dto.WeeklyUpsertRow;

import java.time.LocalDate;
import java.util.List;


public interface ProductMetricWeeklyRepository {

    void save(ProductMetricWeeklyModel productMetricWeeklyModel);

    void bulkUpsert(LocalDate weekStart, LocalDate weekEnd, List<WeeklyUpsertRow> rows);

    void refreshRanks(LocalDate weekStart);

}
