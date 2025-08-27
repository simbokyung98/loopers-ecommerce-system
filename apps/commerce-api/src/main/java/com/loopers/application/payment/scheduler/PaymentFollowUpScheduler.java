package com.loopers.application.payment.scheduler;


import com.loopers.application.payment.dto.ScheduledPayment;

import java.util.Optional;

public interface PaymentFollowUpScheduler {
    /** txKey가 있으면 1분마다 실행, 총 maxAttempts회 */
    void scheduleEveryMinute(Long orderId, Long paymentId, String txKey, int maxAttempts);
    void cancel(Long orderId);

    Optional<ScheduledPayment> findScheduled(Long orderId);
}
