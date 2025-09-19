package com.loopers.interfaces.scheduler;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

@Configuration
@EnableScheduling
@RequiredArgsConstructor
public class MetricJobScheduler {

    private final JobLauncher jobLauncher;
    private final Job metricAggregationJob;

    // 매일 새벽 1시 실행 (주간/월간 집계 동시에 처리)
    @Scheduled(cron = "0 0 1 * * ?")
    public void runMetricAggregationJob() throws Exception {
        JobParameters params = new JobParametersBuilder()
                .addLong("timestamp", System.currentTimeMillis())
                .toJobParameters();

        jobLauncher.run(metricAggregationJob, params);
    }
}
