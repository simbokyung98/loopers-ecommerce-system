package com.loopers.application.dataplatform.port;


import com.loopers.domain.order.event.OrderCreatedEvent;
import com.loopers.domain.payment.event.PaymentCreatedEvent;
import com.loopers.domain.payment.event.PaymentFailedEvent;

public interface DataPlatformGatewayClient {

    void order(OrderCreatedEvent e);

    void payment(PaymentCreatedEvent e);

    void paymentFailed(PaymentFailedEvent e);
}
