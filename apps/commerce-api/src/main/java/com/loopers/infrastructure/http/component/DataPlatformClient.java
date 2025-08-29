package com.loopers.infrastructure.http.component;


import com.loopers.application.dataplatform.port.DataPlatformGatewayClient;
import com.loopers.domain.order.event.OrderCreatedEvent;
import com.loopers.domain.payment.event.PaymentCreatedEvent;
import com.loopers.domain.payment.event.PaymentFailedEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class DataPlatformClient implements DataPlatformGatewayClient {
    @Override
    public void order(OrderCreatedEvent e) {
        log.info("DataPlatform Order {} Post succeeded", e.orderId());

    }

    @Override
    public void payment(PaymentCreatedEvent e) {
        log.info("DataPlatform Payment {} Post succeeded", e.orderId());
    }

    @Override
    public void paymentFailed(PaymentFailedEvent e) {
        log.info("DataPlatform PaymentFailed {} Post succeeded", e.orderId());
    }
}
