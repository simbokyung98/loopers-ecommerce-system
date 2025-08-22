package com.loopers.infrastructure.quartz;


import com.loopers.application.payment.dto.ScheduledPayment;
import com.loopers.application.payment.scheduler.PaymentFollowUpScheduler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.*;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Date;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentFollowUpSchedulerQuartz implements PaymentFollowUpScheduler {

    private static final String GROUP = "payments";
    private final Scheduler scheduler;

    @Override
    public void scheduleEveryMinute(Long orderId, Long paymentId, String txKey, int maxAttempts) {
        try {
            JobKey jobKey = JobKey.jobKey(jobKey(orderId), GROUP);
            if (scheduler.checkExists(jobKey)) {
                scheduler.deleteJob(jobKey); // 재등록 시 교체
            }

            JobDataMap data = new JobDataMap();
            data.put("orderId", orderId);
            data.put("userId", paymentId);
            data.put("txKey", txKey);

            JobDetail job = JobBuilder.newJob(CardPaymentConfirmJob.class)
                    .withIdentity(jobKey)
                    .usingJobData(data)
                    .build();

            Date firstRun = Date.from(Instant.now().plusSeconds(60)); // 1분 뒤 첫 실행
            Trigger trigger = TriggerBuilder.newTrigger()
                    .withIdentity(triggerKey(orderId), GROUP)
                    .forJob(job)
                    .startAt(firstRun)
                    .withSchedule(
                            SimpleScheduleBuilder.simpleSchedule()
                                    .withIntervalInMinutes(1)
                                    .withRepeatCount(Math.max(0, maxAttempts - 1)) // 총 maxAttempts회
                                    .withMisfireHandlingInstructionNextWithRemainingCount()
                    )
                    .build();

            scheduler.scheduleJob(job, trigger);
            log.info("Scheduled every-minute probe: orderId={}, startAt={}, times={}",
                    orderId, firstRun, maxAttempts);
        } catch (SchedulerException e) {
            log.warn("Failed to schedule probe: orderId={}, cause={}", orderId, e.toString());
        }
    }

    @Override
    public void cancel(Long orderId) {
        try {
            scheduler.deleteJob(JobKey.jobKey(jobKey(orderId), GROUP));
            log.info("Canceled probe: orderId={}", orderId);
        } catch (SchedulerException ignore) {}
    }

    @Override
    public Optional<ScheduledPayment> findScheduled(Long orderId) {
        try {
            JobKey key = JobKey.jobKey(jobKey(orderId), GROUP);
            if (!scheduler.checkExists(key)) return Optional.empty();

            JobDetail jd = scheduler.getJobDetail(key);
            if (jd == null) return Optional.empty();

            JobDataMap map = jd.getJobDataMap();
            Long paymentId = (Long) map.get("paymentId");
            String txKey   = map.getString("txKey");

            if (paymentId == null || txKey == null || txKey.isBlank()) return Optional.empty();
            return Optional.of(new ScheduledPayment(paymentId, txKey));

        } catch (SchedulerException e) {
            log.warn("Failed to fetch scheduled context: orderId={}, cause={}", orderId, e.toString());
            return Optional.empty();
        }
    }

    private String jobKey(Long orderId) { return "card-confirm-" + orderId; }
    private String triggerKey(Long orderId) { return "card-confirm-trg-" + orderId; }
}
