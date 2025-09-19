package com.loopers.application;


import com.loopers.application.dto.MonthlyUpsertRow;
import com.loopers.application.dto.ScoredAggregate;
import com.loopers.domain.metric.ProductMetricMonthlyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class MonthlyAggregationWriter implements ItemWriter<ScoredAggregate>, StepExecutionListener {

    private final ProductMetricMonthlyRepository monthlyRepository;

    private LocalDate monthStart;
    private LocalDate monthEnd;

    private final List<ScoredAggregate> buffer = new ArrayList<>();

    @Override
    public void beforeStep(StepExecution stepExecution) {
        this.monthStart = LocalDate.now().minusDays(30);
        this.monthEnd   = LocalDate.now().minusDays(1);
        buffer.clear();
    }

    @Override
    public void write(Chunk<? extends ScoredAggregate> chunk) {
        buffer.addAll(chunk.getItems());
    }

    @Override
    public ExitStatus afterStep(StepExecution stepExecution) {
        if (!buffer.isEmpty()) {
            var rows = buffer.stream()
                    .map(a -> new MonthlyUpsertRow(a.productId(), a.viewSum(), a.likeSum(), a.orderSum(), a.score()))
                    .toList();
            monthlyRepository.bulkUpsert(monthStart, monthEnd, rows);
            monthlyRepository.refreshRanks(monthStart);
        }
        return ExitStatus.COMPLETED;
    }
}

