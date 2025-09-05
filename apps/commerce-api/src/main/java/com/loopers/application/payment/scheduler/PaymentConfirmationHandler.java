package com.loopers.application.payment.scheduler;

import com.loopers.application.order.OrderFacade;
import com.loopers.application.order.dto.OrderInfo;
import com.loopers.application.payment.PaymentGatewayService;
import com.loopers.application.payment.dto.PaymentProbe;
import com.loopers.application.payment.dto.ScheduledPayment;
import com.loopers.application.payment.event.ConfirmedOrderItem;
import com.loopers.application.payment.event.PaymentConfirmedEvent;
import com.loopers.application.payment.event.PaymentFailedEvent;
import com.loopers.confg.kafka.KafkaMessage;
import com.loopers.domain.payment.PaymentService;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentConfirmationHandler implements PaymentFollowUpUseCase {

    private final PaymentGatewayService paymentGatewayService; // PG 확인
    private final PaymentService paymentService;
    private final OrderFacade orderFacade;
    private final PaymentFollowUpScheduler scheduler;          // 성공 시 반복 중지
    private final KafkaTemplate<Object, Object> kafkaTemplate;

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

                OrderInfo.OrderDetail response = orderFacade.getOrder(orderId);

                List<ConfirmedOrderItem> items = response.orderItems().stream()
                                .map(i -> new ConfirmedOrderItem(i.productId(), i.quantity()))
                                        .toList();
                PaymentConfirmedEvent paymentConfirmedEvent = new PaymentConfirmedEvent(
                        orderId, paymentId, response.userId(), items
                );

                KafkaMessage<PaymentConfirmedEvent> message = KafkaMessage.from(paymentConfirmedEvent);
                kafkaTemplate.send("product.payment.confirmed.v1",String.valueOf(paymentId), message );


                scheduler.cancel(orderId);

            }
            case STOP -> {
                paymentService.failedPay(paymentId);
                orderFacade.failedPayment(orderId);

                OrderInfo.OrderDetail response = orderFacade.getOrder(orderId);

                KafkaMessage<PaymentFailedEvent> message = KafkaMessage.from(
                        new PaymentFailedEvent(paymentId, paymentId, response.userId())
                );

                kafkaTemplate.send("product.payment.failed.v1",String.valueOf(paymentId), message );


                scheduler.cancel(orderId);
            }
            case RETRY -> {
                log.info("Payment not confirmed yet (retry): orderId={}", orderId);
            }
        }
    }
}
