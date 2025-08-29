package com.loopers.application.dataplatform;


import com.loopers.application.dataplatform.port.DataPlatformGatewayClient;
import com.loopers.domain.order.event.OrderCreatedEvent;
import com.loopers.domain.payment.event.PaymentCreatedEvent;
import com.loopers.domain.payment.event.PaymentFailedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
@Slf4j
@Component
@RequiredArgsConstructor
public class DataPlatformEventHandler {

    private final DataPlatformGatewayClient dataPlatformGatewayClient;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleOrderCreated(OrderCreatedEvent e) {
        dataPlatformGatewayClient.order(e);
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleOrderCreated(PaymentCreatedEvent e) {
        dataPlatformGatewayClient.payment(e);
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleOrderCreated(PaymentFailedEvent e) {
        dataPlatformGatewayClient.paymentFailed(e);
    }
}
