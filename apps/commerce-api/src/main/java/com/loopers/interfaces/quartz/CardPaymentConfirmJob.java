package com.loopers.interfaces.quartz;


import com.loopers.application.payment.scheduler.PaymentFollowUpUseCase;
import lombok.extern.slf4j.Slf4j;
import org.quartz.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.quartz.QuartzJobBean;

@Slf4j
@DisallowConcurrentExecution
public class CardPaymentConfirmJob extends QuartzJobBean {

    @Autowired
    private PaymentFollowUpUseCase followUpUseCase; // 포트만 의존

    @Override
    protected void executeInternal(JobExecutionContext ctx) throws JobExecutionException {
        JobDataMap data = ctx.getMergedJobDataMap();
        Long orderId = data.getLong("orderId");
        Long paymentId  = data.getLong("paymentId");
        String txKey = data.getString("txKey");

        try {
            followUpUseCase.onCardPendingTick(orderId, paymentId, txKey);
        } catch (Exception e) {
            throw new JobExecutionException(e, false);
        }
    }
}
