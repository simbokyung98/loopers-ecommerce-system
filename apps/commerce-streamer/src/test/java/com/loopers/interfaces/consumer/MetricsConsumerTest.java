package com.loopers.interfaces.consumer;

import com.loopers.application.metric.MetricsAggregationFacade;
import com.loopers.application.metric.dto.*;
import com.loopers.application.ranking.RankingFacade;
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

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MetricsConsumerTest {

    @Mock
    private MetricsAggregationFacade metricsAggregationFacade;

    @Mock
    private RankingFacade rankingFacade;

    @InjectMocks
    private MetricsConsumer consumer;

    @Test
    @DisplayName("onLikeEvents - CREATED 이벤트 시 집계 + 랭킹 갱신 + ack 호출")
    void onLikeEvents_created() {
        LikeEvent likeEvent = new LikeEvent(1L, 100L, "cacheKey", LikeEventType.CREATED);
        KafkaMessage<LikeEvent> message = new KafkaMessage<>("event1", "2025-09-12T10:00:00", likeEvent);
        Acknowledgment ack = mock(Acknowledgment.class);

        consumer.onLikeEvents(List.of(message), ack);

        verify(metricsAggregationFacade).aggregateLikes(anyList());
        verify(rankingFacade).updateLike(100L, 1);
        verify(ack).acknowledge();
    }

    @Test
    @DisplayName("onLikeEvents - DELETED 이벤트 시 집계만, 랭킹 갱신은 없음")
    void onLikeEvents_deleted() {
        LikeEvent likeEvent = new LikeEvent(1L, 200L, "cacheKey", LikeEventType.DELETED);
        KafkaMessage<LikeEvent> message = new KafkaMessage<>("event2", "2025-09-12T10:05:00", likeEvent);
        Acknowledgment ack = mock(Acknowledgment.class);

        consumer.onLikeEvents(List.of(message), ack);

        verify(metricsAggregationFacade).aggregateLikes(anyList());
        verify(rankingFacade, never()).updateLike(anyLong(), anyInt());
        verify(ack).acknowledge();
    }

    @Test
    @DisplayName("onOrderCreated - 주문 이벤트 시 집계 + 랭킹 갱신 + ack 호출")
    void onOrderCreated() {
        PaymentConfirmedEvent event = mock(PaymentConfirmedEvent.class);
        when(event.toOrderItemList()).thenReturn(List.of(
                new OrderItem(10L, 1L),
                new OrderItem(20L, 1L)
        ));
        KafkaMessage<PaymentConfirmedEvent> message = new KafkaMessage<>("event3", "2025-09-12T11:00:00", event);
        Acknowledgment ack = mock(Acknowledgment.class);

        consumer.onOrderCreated(List.of(message), ack);

        verify(metricsAggregationFacade).aggregateOrders(anyList());
        verify(rankingFacade).updateOrder(10L, 1);
        verify(rankingFacade).updateOrder(20L, 1);
        verify(ack).acknowledge();
    }

    @Test
    @DisplayName("onProductViewed - 조회 이벤트 시 집계 + 랭킹 갱신 + ack 호출")
    void onProductViewed() {
        ProductViewedEvent event = mock(ProductViewedEvent.class);
        when(event.to(any())).thenReturn(new ProductMetricEventCriteria(
                new EventMessage("event4", "2025-09-12T12:00:00"),
                300L
        ));
        KafkaMessage<ProductViewedEvent> message = new KafkaMessage<>("event4", "2025-09-12T12:00:00", event);
        Acknowledgment ack = mock(Acknowledgment.class);

        consumer.onProductViewed(List.of(message), ack);

        verify(metricsAggregationFacade).aggregateViews(anyList());
        verify(rankingFacade).updateView(300L, 1);
        verify(ack).acknowledge();
    }
}
