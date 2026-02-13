package com.loopers.application;

import com.loopers.application.dto.ScoredAggregate;
import com.loopers.domain.metric.ProductMetricMonthlyRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.batch.item.Chunk;

import java.time.LocalDate;

import static org.mockito.Mockito.*;

class MonthlyAggregationWriterTest {

    private ProductMetricMonthlyRepository monthlyRepository;
    private MonthlyAggregationWriter writer;

    @BeforeEach
    void setUp() {
        monthlyRepository = mock(ProductMetricMonthlyRepository.class);
        writer = new MonthlyAggregationWriter(monthlyRepository);
    }

    @Test
    void writer_buffers_items_and_flushes_on_afterStep() {
        // given
        ScoredAggregate a1 = new ScoredAggregate(1L, 10, 5, 2, 10*1 + 5*2 + 2*5);
        ScoredAggregate a2 = new ScoredAggregate(2L, 3, 1, 1, 3*1 + 1*2 + 1*5);

        writer.beforeStep(null);

        // when
        writer.write(Chunk.of(a1, a2));
        writer.afterStep(null);

        // then
        verify(monthlyRepository, times(1))
                .bulkUpsert(any(LocalDate.class), any(LocalDate.class), anyList());
        verify(monthlyRepository, times(1))
                .refreshRanks(any(LocalDate.class));
    }

    @Test
    void writer_does_not_call_repository_when_buffer_is_empty() {
        // given
        writer.beforeStep(null);

        // when
        writer.afterStep(null);

        // then
        verify(monthlyRepository, never()).bulkUpsert(any(), any(), anyList());
        verify(monthlyRepository, never()).refreshRanks(any());
    }
}
