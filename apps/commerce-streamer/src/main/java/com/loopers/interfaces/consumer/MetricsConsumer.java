package com.loopers.interfaces.consumer;

import com.loopers.application.metric.MetricsAggregationFacade;
import com.loopers.application.metric.dto.*;
import com.loopers.confg.kafka.KafkaMessage;
import com.loopers.interfaces.consumer.event.LikeEvent;
import com.loopers.interfaces.consumer.event.PaymentConfirmedEvent;
import com.loopers.interfaces.consumer.event.ProductViewedEvent;
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

    private final MetricsAggregationFacade metricsAggregationFacade;

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

        List<LikeMetricEventCriteria> criteriaList = events.stream()
                        .map(item -> {
                            EventMessage message = new EventMessage(item.eventId(), item.publishedAt());
                            LikeEvent likeEvent = item.payload();

                            return likeEvent.to(message);
                        }).toList();

        metricsAggregationFacade.aggregateLikes(criteriaList);

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

        List<OrderMetricEventCriteria> criteriaList = events.stream()
                .map(item -> {
                    EventMessage message = new EventMessage(item.eventId(), item.publishedAt());
                    List<OrderItem> orderItemList = item.payload().toOrderItemList();
                    return new OrderMetricEventCriteria(message, orderItemList);
                }).toList();

        metricsAggregationFacade.aggregateOrders(criteriaList);

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

        List<ProductMetricEventCriteria> criteriaList = events.stream()
                .map(item -> {
                    EventMessage message = new EventMessage(item.eventId(), item.publishedAt());
                    ProductViewedEvent productViewedEvent = item.payload();

                    return productViewedEvent.to(message);
                }).toList();

        metricsAggregationFacade.aggregateViews(criteriaList);

        ack.acknowledge();
    }

}
