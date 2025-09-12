package com.loopers.interfaces.scheduler;


import com.loopers.application.ranking.RankingWeightFacade;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobExecutionContext;
import org.springframework.scheduling.quartz.QuartzJobBean;

import java.time.LocalDate;

@Slf4j
@DisallowConcurrentExecution
@RequiredArgsConstructor
public class RankingPreWarmJob extends QuartzJobBean {

    private final RankingWeightFacade rankingWeightFacade;

    @Override
    protected void executeInternal(JobExecutionContext context) {
        try {
            LocalDate today = LocalDate.now();
            LocalDate tomorrow = today.plusDays(1);

            log.info("[Scheduler] RankingPreWarmJob 실행 ({} → {})", today, tomorrow);

            rankingWeightFacade.carryOver(tomorrow,0.1);


            log.info("[Scheduler] RankingPreWarmJob 완료: 내일 키 미리 생성됨.");
        } catch (Exception e) {
            log.error("[Scheduler] RankingPreWarmJob 실패", e);
        }
    }
}
