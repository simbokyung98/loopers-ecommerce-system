package com.loopers.application;

import com.loopers.application.dto.AggregateRow;
import jakarta.persistence.EntityManagerFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.database.JpaPagingItemReader;
import org.springframework.batch.item.database.builder.JpaPagingItemReaderBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDate;
import java.util.Map;

@Configuration
@RequiredArgsConstructor
public class MetricReaders {

    private final EntityManagerFactory managerFactory;

    @Bean
    @StepScope
    public JpaPagingItemReader<AggregateRow> weeklyReader() {
        LocalDate start = LocalDate.now().minusDays(7);
        LocalDate end   = LocalDate.now().minusDays(1);

        return new JpaPagingItemReaderBuilder<AggregateRow>()
                .name("weeklyReader")
                .entityManagerFactory(managerFactory)
                .queryString("""
                    select new com.loopers.application.dto.AggregateRow(
                        d.productId,
                        sum(d.viewCount),
                        sum(d.likeCount),
                        sum(d.saleCount)
                    )
                    from ProductMetricDailyModel d
                    where d.date between :start and :end
                    group by d.productId
                """)
                .parameterValues(Map.of("start", start, "end", end))
                .pageSize(1000)
                .build();
    }

    @Bean
    @StepScope
    public JpaPagingItemReader<AggregateRow> monthlyReader() {
        LocalDate start = LocalDate.now().minusDays(30);
        LocalDate end   = LocalDate.now().minusDays(1);

        return new JpaPagingItemReaderBuilder<AggregateRow>()
                .name("monthlyReader")
                .entityManagerFactory(managerFactory)
                .queryString("""
                    select new com.loopers.application.dto.AggregateRow(
                        d.productId,
                        sum(d.viewCount),
                        sum(d.likeCount),
                        sum(d.saleCount)
                    )
                    from ProductMetricDailyModel d
                    where d.date between :start and :end
                    group by d.productId
                """)
                .parameterValues(Map.of("start", start, "end", end))
                .pageSize(1000)
                .build();
    }
}
