package com.loopers.interfaces.consumer;

import com.loopers.confg.kafka.KafkaMessage;
import com.loopers.domain.event.EventHandlerService;
import com.loopers.domain.metric.ProductMetricDailyService;
import com.loopers.interfaces.consumer.event.ConfirmedOrderItem;
import com.loopers.interfaces.consumer.event.LikeEvent;
import com.loopers.interfaces.consumer.event.PaymentConfirmedEvent;
import com.loopers.interfaces.consumer.event.ProductViewedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class MetricsConsumer {

    private final EventHandlerService eventHandlerService;
    private final ProductMetricDailyService productMetricDailyService;

    /**
     * 좋아요 생성 집계
     */
    @KafkaListener
            (
            topics = "${demo-kafka.like.topic-name}",
            groupId = "metrics-consumer-group",
            containerFactory = "metricsKafkaListenerContainerFactory"
    )
    public void onLikeEvents(List<KafkaMessage<LikeEvent>> events, Acknowledgment ack) {

        System.out.println("Received like events"+ events.size());

        events.forEach(e -> System.out.println("EventId="+e.eventId()+", Payload= " + e.payload()));



        events.stream()
                .filter(e -> eventHandlerService.tryConsume(e.eventId(), "metrics"))
                .map(KafkaMessage::payload)
                .forEach(payload ->
                        productMetricDailyService.updateLike(payload.productId(), payload.type().delta())
                );

        ack.acknowledge();
    }


    /**
     * 주문 생성 이벤트
     */
    @KafkaListener(
            topics = "${demo-kafka.payment.confirmed.topic-name}",
            groupId = "metrics-consumer-group",
            containerFactory = "metricsKafkaListenerContainerFactory"
    )
    public void onOrderCreated(List<KafkaMessage<PaymentConfirmedEvent>> events, Acknowledgment ack) {
        // 상품별 판매 수량 합산
        Map<Long, Integer> saleCounts = events.stream()
                .filter(e -> eventHandlerService.tryConsume(e.eventId(), "metrics"))
                .flatMap(e -> e.payload().items().stream())
                .collect(Collectors.groupingBy(
                        ConfirmedOrderItem::productId,
                        Collectors.summingInt(item -> item.quantity().intValue())
                ));

        // DB 반영은 상품별 1번만
        saleCounts.forEach(productMetricDailyService::updateSale);

        ack.acknowledge();
    }

    /**
     * 상품 조회 이벤트 → viewCount +1
     */
    @KafkaListener(
            topics = "${demo-kafka.product.viewed.topic-name}",
            groupId = "metrics-consumer-group",
            containerFactory = "metricsKafkaListenerContainerFactory"
    )
    public void onProductViewed(List<KafkaMessage<ProductViewedEvent>> events, Acknowledgment ack) {
        Map<Long, Long> viewCounts = events.stream()
                .filter(e -> eventHandlerService.tryConsume(e.eventId(), "metrics"))
                .map(KafkaMessage::payload)
                .collect(Collectors.groupingBy(
                        ProductViewedEvent::productId,
                        Collectors.counting()
                ));

        // 상품별 증가량 반영
        viewCounts.forEach((productId, count) ->
                productMetricDailyService.updateView(productId, count.intValue())
        );

        ack.acknowledge();
    }

}
