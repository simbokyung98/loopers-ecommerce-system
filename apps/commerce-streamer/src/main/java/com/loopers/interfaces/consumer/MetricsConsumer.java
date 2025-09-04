package com.loopers.interfaces.consumer;

import com.loopers.confg.kafka.KafkaMessage;
import com.loopers.domain.event.EventHandlerService;
import com.loopers.domain.metric.ProductMetricDailyService;
import com.loopers.interfaces.consumer.event.LikeEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

import java.util.List;

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
        events.stream()
                .filter(e -> eventHandlerService.tryConsume(e.eventId(), "metrics"))
                .map(KafkaMessage::payload)
                .forEach(payload ->
                        productMetricDailyService.updateLike(payload.productId(), payload.type().delta())
                );

        ack.acknowledge();
    }


//    /**
//     * 주문 생성 이벤트
//     */
//    @KafkaListener(
//            topics = "${demo-kafka.order.created.topic-name}",
//            groupId = "metrics-consumer-group",
//            containerFactory = "metricsKafkaListenerContainerFactory"
//    )
//    public void onOrderCreated(List<KafkaMessage<OrderCreatedEvent>> events, Acknowledgment ack) {
//        events.stream()
//                .filter(e -> eventHandlerService.tryConsume(e.eventId(), "metrics"))
//                .map(e -> e.payload().productId())
//                .forEach(productId -> dailyService.updateSaleCount(productId, +1));
//
//        ack.acknowledge();
//    }
//
//    /**
//     * 상품 조회 이벤트 → viewCount +1
//     */
//    @KafkaListener(
//            topics = "${demo-kafka.product.viewed.topic-name}",
//            groupId = "metrics-consumer-group",
//            containerFactory = "metricsKafkaListenerContainerFactory"
//    )
//    public void onProductViewed(List<KafkaMessage<ProductViewedEvent>> events, Acknowledgment ack) {
//        events.stream()
//                .filter(e -> eventHandlerService.tryConsume(e.eventId(), "metrics"))
//                .map(e -> e.payload().productId())
//                .forEach(productId -> dailyService.updateViewCount(productId, +1));
//
//        ack.acknowledge();
//    }

}
