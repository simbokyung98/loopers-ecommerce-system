package com.loopers.interfaces.consumer;


import com.loopers.confg.kafka.KafkaMessage;
import com.loopers.domain.event.EventHandlerService;
import com.loopers.domain.metric.ProductMetricDailyService;
import com.loopers.interfaces.consumer.MetricsConsumer;
import com.loopers.interfaces.consumer.event.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.support.Acknowledgment;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MetricsConsumerTest {

    @Mock
    private EventHandlerService eventHandlerService;

    @Mock
    private ProductMetricDailyService productMetricDailyService;

    @InjectMocks
    private MetricsConsumer consumer;

    @Test
    @DisplayName("좋아요 이벤트 발생 시: likeCount 업데이트 호출")
    void onLikeEvents_shouldUpdateLikeCount() {
        // given
        LikeEvent likeEvent = new LikeEvent(1L, 100L, null, LikeEventType.CREATED);
        KafkaMessage<LikeEvent> message = KafkaMessage.from(likeEvent);

        when(eventHandlerService.tryConsume(any(), eq("metrics"))).thenReturn(true);

        // when
        consumer.onLikeEvents(List.of(message), mock(Acknowledgment.class));

        // then
        verify(productMetricDailyService).updateLike(100L, likeEvent.type().delta());
    }

    @Test
    @DisplayName("주문 생성 이벤트 발생 시: 상품별 판매 수량 합산하여 updateSale 호출")
    void onOrderCreated_shouldUpdateSaleCounts() {
        // given
        ConfirmedOrderItem item1 = new ConfirmedOrderItem(200L, 2L);
        ConfirmedOrderItem item2 = new ConfirmedOrderItem(200L, 3L);
        ConfirmedOrderItem item3 = new ConfirmedOrderItem(300L, 1L);

        PaymentConfirmedEvent paymentEvent = new PaymentConfirmedEvent(1L,2L, 3L, List.of(item1, item2, item3));
        KafkaMessage<PaymentConfirmedEvent> message = KafkaMessage.from(paymentEvent);

        when(eventHandlerService.tryConsume(any(), eq("metrics"))).thenReturn(true);

        // when
        consumer.onOrderCreated(List.of(message), mock(Acknowledgment.class));

        // then
        verify(productMetricDailyService).updateSale(200L, 5); // 2+3
        verify(productMetricDailyService).updateSale(300L, 1);
    }

    @Test
    @DisplayName("상품 조회 이벤트 발생 시: 상품별 조회수 합산하여 updateView 호출")
    void onProductViewed_shouldUpdateViewCounts() {
        // given
        ProductViewedEvent e1 = new ProductViewedEvent(400L);
        ProductViewedEvent e2 = new ProductViewedEvent(400L);
        ProductViewedEvent e3 = new ProductViewedEvent(500L);

        KafkaMessage<ProductViewedEvent> m1 = KafkaMessage.from(e1);
        KafkaMessage<ProductViewedEvent> m2 = KafkaMessage.from(e2);
        KafkaMessage<ProductViewedEvent> m3 = KafkaMessage.from(e3);

        when(eventHandlerService.tryConsume(any(), eq("metrics"))).thenReturn(true);

        // when
        consumer.onProductViewed(List.of(m1, m2, m3), mock(Acknowledgment.class));

        // then
        verify(productMetricDailyService).updateView(400L, 2);
        verify(productMetricDailyService).updateView(500L, 1);
    }
}
