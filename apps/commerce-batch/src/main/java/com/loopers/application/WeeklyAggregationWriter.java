package com.loopers.application;

import com.loopers.application.dto.ScoredAggregate;
import com.loopers.application.dto.WeeklyUpsertRow;
import com.loopers.domain.metric.ProductMetricWeeklyRepository;
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
public class WeeklyAggregationWriter implements ItemWriter<ScoredAggregate>, StepExecutionListener {

    private final ProductMetricWeeklyRepository weeklyRepository;

    private LocalDate weekStart;
    private LocalDate weekEnd;

    private final List<ScoredAggregate> buffer = new ArrayList<>();

    @Override
    public void beforeStep(StepExecution stepExecution) {
        // 필요시 JobParameter로 교체 가능
        this.weekStart = LocalDate.now().minusDays(7);
        this.weekEnd   = LocalDate.now().minusDays(1);
        buffer.clear();
    }

    @Override
    public void write(Chunk<? extends ScoredAggregate> chunk) {
        buffer.addAll(chunk.getItems()); // 청크 모아 일괄 업서트
    }

    @Override
    public ExitStatus afterStep(StepExecution stepExecution) {
        if (!buffer.isEmpty()) {
            var rows = buffer.stream()
                    .map(a -> new WeeklyUpsertRow(a.productId(), a.viewSum(), a.likeSum(), a.orderSum(), a.score()))
                    .toList();
            weeklyRepository.bulkUpsert(weekStart, weekEnd, rows);
            weeklyRepository.refreshRanks(weekStart);
        }
        return ExitStatus.COMPLETED;
    }
}
