package com.loopers.application.payment;

import com.loopers.application.order.OrderFacade;
import com.loopers.application.order.dto.OrderInfo;
import com.loopers.application.payment.dto.PaymentCriteria;
import com.loopers.application.payment.event.ConfirmedOrderItem;
import com.loopers.cache.ProductDetailCache;
import com.loopers.confg.kafka.KafkaMessage;
import com.loopers.domain.order.event.OrderCreatedEvent;
import com.loopers.domain.order.event.OrderCreatedKafkaEvent;
import com.loopers.domain.product.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentEventHandler {

    private final PaymentFacade paymentFacade;
    private final OrderFacade orderFacade;
    private final ProductService productService;
    private final KafkaTemplate<Object, Object> kafkaTemplate;
    private final ProductDetailCache productDetailCache;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleOrderCreated(OrderCreatedEvent e) {
        PaymentCriteria.CreatePayment criteria = new PaymentCriteria.CreatePayment(
                e.orderId(),e.userId(), e.paymentType(), e.cardType(), e.cardNo(), e.amount()
        );

        OrderInfo.OrderDetail response = orderFacade.getOrder(e.orderId());

        List<ConfirmedOrderItem> items = response.orderItems().stream()
                .map(i -> new ConfirmedOrderItem(i.productId(), i.quantity()))
                .toList();

        items.stream().filter(i -> productService.isOutOfStock(i.productId()))
                .forEach(i -> productDetailCache.evict(i.productId()));

        OrderCreatedKafkaEvent orderKafkaEvent = new OrderCreatedKafkaEvent(e.orderId(), e.userId(), items);

        KafkaMessage<OrderCreatedKafkaEvent> message = KafkaMessage.from(orderKafkaEvent);
        kafkaTemplate.send("product.order.create.v1",String.valueOf(e.orderId()), message );

        try {
            paymentFacade.pay(criteria);
            orderFacade.completePayment(e.orderId());
            log.info("Order {} payment succeeded", e.orderId());
        } catch (RuntimeException ex) {
            orderFacade.failedPayment(e.orderId());
            log.warn("Order {} payment failed: {}", e.orderId(), ex.getMessage());

            kafkaTemplate.send("product.order.failed.v1",String.valueOf(e.orderId()), message );
        }
    }
}
