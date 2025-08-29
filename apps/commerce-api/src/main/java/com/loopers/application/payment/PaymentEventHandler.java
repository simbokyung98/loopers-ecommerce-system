package com.loopers.application.payment;

import com.loopers.application.order.OrderFacade;
import com.loopers.application.payment.dto.PaymentCriteria;
import com.loopers.domain.order.event.OrderCreatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentEventHandler {

    private final PaymentFacade paymentFacade;
    private final OrderFacade orderFacade;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleOrderCreated(OrderCreatedEvent e) {
        PaymentCriteria.CreatePayment criteria = new PaymentCriteria.CreatePayment(
                e.orderId(),e.userId(), e.paymentType(), e.cardType(), e.cardNo(), e.amount()
        );

        try {
            paymentFacade.pay(criteria);
            orderFacade.completePayment(e.orderId());
            log.info("Order {} payment succeeded", e.orderId());
        } catch (RuntimeException ex) {
            orderFacade.failedPayment(e.orderId());
            log.warn("Order {} payment failed: {}", e.orderId(), ex.getMessage());
        }
    }
}
