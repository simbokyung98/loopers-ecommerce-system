package com.loopers.interfaces.scheduler;

import org.quartz.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class QuartzConfig {

    @Bean
    public JobDetail rankingPreWarmJobDetail() {
        return JobBuilder.newJob(RankingPreWarmJob.class)
                .withIdentity("rankingPreWarmJob")
                .storeDurably()
                .build();
    }

    @Bean
    public Trigger rankingPreWarmTrigger() {
        return TriggerBuilder.newTrigger()
                .forJob(rankingPreWarmJobDetail())
                .withIdentity("rankingPreWarmTrigger")
                .withSchedule(CronScheduleBuilder.dailyAtHourAndMinute(23, 50))
                .build();
    }
}
