package com.loopers.application.payment.scheduler;

import com.loopers.application.order.OrderFacade;
import com.loopers.application.payment.PaymentGatewayService;
import com.loopers.application.payment.dto.PaymentProbe;
import com.loopers.application.payment.dto.ScheduledPayment;
import com.loopers.domain.payment.PaymentService;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentConfirmationHandler implements PaymentFollowUpUseCase {

    private final PaymentGatewayService paymentGatewayService; // PG 확인
    private final PaymentService paymentService;
    private final OrderFacade orderFacade;
    private final PaymentFollowUpScheduler scheduler;          // 성공 시 반복 중지

    @Override
    public void onCardPendingTick(Long orderId, Long paymentId, String txKey) {

        PaymentProbe probe = paymentGatewayService.checkPayment(txKey);
        handleProbe(orderId, paymentId, probe);


    }

    @Override
    public void onCardPaymentCallback(Long orderId, String txKey) {

        //callback api 검증
        ScheduledPayment scheduledPayment = scheduler.findScheduled(orderId)
                .orElseThrow(() -> {
                    log.warn("Reject callback: no scheduled job. orderId={}, callbackTxKey={}", orderId, txKey);
                    return new CoreException(ErrorType.BAD_REQUEST, "유효하지 않은 결제 콜백(등록된 진행 없음)");
                });

        if(!scheduledPayment.txKey().equals(txKey)){
            log.warn("Reject callback: no scheduled job. orderId={}, callbackTxKey={}", orderId, txKey);
            throw new CoreException(ErrorType.BAD_REQUEST, "유효하지 않은 결제 콜백(등록된 진행 없음)");
        }

        PaymentProbe probe = paymentGatewayService.checkPayment(txKey);
        handleProbe(orderId, scheduledPayment.paymentId(), probe);


    }

    private void handleProbe(Long orderId, Long paymentId, PaymentProbe probe) {
        switch (probe.decision()) {
            case CONFIRMED -> {
                paymentService.completePay(paymentId);
                scheduler.cancel(orderId);
            }
            case STOP -> {
                paymentService.failedPay(paymentId);
                orderFacade.failedPayment(orderId);
                scheduler.cancel(orderId);
            }
            case RETRY -> {
                log.info("Payment not confirmed yet (retry): orderId={}", orderId);
            }
        }
    }
}
