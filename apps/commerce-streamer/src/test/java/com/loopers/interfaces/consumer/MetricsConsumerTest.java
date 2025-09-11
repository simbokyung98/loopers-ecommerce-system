package com.loopers.interfaces.consumer;

import com.loopers.application.metric.MetricsAggregationFacade;
import com.loopers.application.metric.dto.*;
import com.loopers.confg.kafka.KafkaMessage;
import com.loopers.interfaces.consumer.event.LikeEvent;
import com.loopers.interfaces.consumer.event.LikeEventType;
import com.loopers.interfaces.consumer.event.PaymentConfirmedEvent;
import com.loopers.interfaces.consumer.event.ProductViewedEvent;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.support.Acknowledgment;

import java.util.List;

import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MetricsConsumerTest {

    @Mock
    private MetricsAggregationFacade metricsAggregationFacade;

    @InjectMocks
    private MetricsConsumer consumer;

    @Test
    @DisplayName("onLikeEvents → aggregateLikes + ack 호출")
    void onLikeEvents_shouldCallAggregateLikesAndAck() {
        // given
        LikeEvent likeEvent = new LikeEvent(1L, 100L, "cacheKey", LikeEventType.CREATED);
        KafkaMessage<LikeEvent> message = new KafkaMessage<>("event1", "2025-09-12T10:00:00", likeEvent);
        Acknowledgment ack = mock(Acknowledgment.class);

        // when
        consumer.onLikeEvents(List.of(message), ack);

        // then
        verify(metricsAggregationFacade).aggregateLikes(anyList());
        verify(ack).acknowledge();
    }

    @Test
    @DisplayName("onOrderCreated → aggregateOrders + ack 호출")
    void onOrderCreated_shouldCallAggregateOrdersAndAck() {
        // given
        PaymentConfirmedEvent event = mock(PaymentConfirmedEvent.class);
        when(event.toOrderItemList()).thenReturn(List.of(new OrderItem(10L, 2L)));
        KafkaMessage<PaymentConfirmedEvent> message = new KafkaMessage<>("event2", "2025-09-12T11:00:00", event);
        Acknowledgment ack = mock(Acknowledgment.class);

        // when
        consumer.onOrderCreated(List.of(message), ack);

        // then
        verify(metricsAggregationFacade).aggregateOrders(anyList());
        verify(ack).acknowledge();
    }

    @Test
    @DisplayName("onProductViewed → aggregateViews + ack 호출")
    void onProductViewed_shouldCallAggregateViewsAndAck() {
        // given
        ProductViewedEvent event = mock(ProductViewedEvent.class);
        when(event.to(any())).thenReturn(new ProductMetricEventCriteria(new EventMessage("event3", "2025-09-12T12:00:00"), 300L));

        KafkaMessage<ProductViewedEvent> message = new KafkaMessage<>("event3", "2025-09-12T12:00:00", event);
        Acknowledgment ack = mock(Acknowledgment.class);

        // when
        consumer.onProductViewed(List.of(message), ack);

        // then
        verify(metricsAggregationFacade).aggregateViews(anyList());
        verify(ack).acknowledge();
    }
}
