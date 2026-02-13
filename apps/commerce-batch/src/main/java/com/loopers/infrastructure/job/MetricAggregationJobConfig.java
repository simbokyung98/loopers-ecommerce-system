package com.loopers.infrastructure.job;

import com.loopers.application.MetricReaders;
import com.loopers.application.MonthlyAggregationWriter;
import com.loopers.application.WeeklyAggregationWriter;
import com.loopers.application.MetricPassThroughProcessor;
import com.loopers.application.dto.AggregateRow;
import com.loopers.application.dto.ScoredAggregate;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
@RequiredArgsConstructor
public class MetricAggregationJobConfig {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager txManager;

    private final MetricPassThroughProcessor processor;
    private final WeeklyAggregationWriter weeklyWriter;
    private final MonthlyAggregationWriter monthlyWriter;

    private final MetricReaders readers;

    @Bean
    public Step weeklyStep() {
        return new StepBuilder("weeklyStep", jobRepository)
                .<AggregateRow, ScoredAggregate>chunk(1000, txManager)
                .reader(readers.weeklyReader())
                .processor(processor)
                .writer(weeklyWriter)
                .build();
    }

    @Bean
    public Step monthlyStep() {
        return new StepBuilder("monthlyStep", jobRepository)
                .<AggregateRow, ScoredAggregate>chunk(1000, txManager)
                .reader(readers.monthlyReader())
                .processor(processor)
                .writer(monthlyWriter)
                .build();
    }

    @Bean
    public Job metricAggregationJob(Step weeklyStep, Step monthlyStep) {
        return new JobBuilder("metricAggregationJob", jobRepository)
                .start(weeklyStep)
                .next(monthlyStep)
                .build();
    }
}
