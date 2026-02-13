package com.loopers.infrastructure.job;


import com.loopers.domain.metric.ProductMetricDailyModel;
import com.loopers.domain.metric.ProductMetricMonthlyModel;
import com.loopers.domain.metric.ProductMetricWeeklyModel;
import com.loopers.infrastructure.ProductMetricDailyJpaRepository;
import com.loopers.infrastructure.ProductMetricMonthlyJpaRepository;
import com.loopers.infrastructure.ProductMetricWeeklyJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.batch.test.context.SpringBatchTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBatchTest
@SpringBootTest
class MetricAggregationJobConfigTest {

    @Autowired private JobLauncherTestUtils jobLauncherTestUtils;


    @Autowired private Job metricAggregationJob;

    @Autowired private ProductMetricDailyJpaRepository dailyRepo;
    @Autowired private ProductMetricWeeklyJpaRepository weeklyRepo;
    @Autowired private ProductMetricMonthlyJpaRepository monthlyRepo;

    @BeforeEach
    void setUp() {

        weeklyRepo.deleteAll();
        monthlyRepo.deleteAll();
        dailyRepo.deleteAll();


        LocalDate d1 = LocalDate.now().minusDays(1);
        LocalDate d2 = LocalDate.now().minusDays(2);
        dailyRepo.saveAll(List.of(
                new ProductMetricDailyModel(1001L, d1),
                new ProductMetricDailyModel(1002L, d1),
                new ProductMetricDailyModel(1001L, d2)
        ));
    }

    @Test
    void metricAggregationJob_runs_successfully() throws Exception {

        jobLauncherTestUtils.setJob(metricAggregationJob);

        JobExecution jobExecution = jobLauncherTestUtils.launchJob();
        assertThat(jobExecution.getStatus()).isEqualTo(BatchStatus.COMPLETED);

        // 주간/월간 집계가 생겼는지 확인
        List<ProductMetricWeeklyModel> weekly = weeklyRepo.findAll();
        List<ProductMetricMonthlyModel> monthly = monthlyRepo.findAll();

        assertThat(weekly).isNotEmpty();
        assertThat(monthly).isNotEmpty();


        assertThat(weekly).allMatch(w -> w.getScore() >= 0);
        assertThat(weekly).allMatch(w -> w.getRankValue() > 0);
    }

    @Test
    void weeklyStep_runs_successfully() throws Exception {

        JobExecution jobExecution = jobLauncherTestUtils.launchStep("weeklyStep");
        assertThat(jobExecution.getStatus()).isEqualTo(BatchStatus.COMPLETED);

        List<ProductMetricWeeklyModel> weekly = weeklyRepo.findAll();
        assertThat(weekly).isNotEmpty();
    }
}
