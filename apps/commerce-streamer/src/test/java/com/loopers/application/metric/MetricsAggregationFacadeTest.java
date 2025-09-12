package com.loopers.application.metric;

import com.loopers.application.metric.dto.*;
import com.loopers.domain.event.EventHandlerService;
import com.loopers.domain.metric.ProductMetricDailyService;
import com.loopers.interfaces.consumer.event.LikeEventType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MetricsAggregationFacadeTest {

    @Mock
    private EventHandlerService eventHandlerService;

    @Mock
    private ProductMetricDailyService productMetricDailyService;

    @InjectMocks
    private MetricsAggregationFacade metricsAggregationFacade;

    @Test
    @DisplayName("aggregateLikes: CREATED 이벤트 → like 수 증가")
    void aggregateLikes_created() {
        EventMessage eventMessage = new EventMessage("event1", "2025-09-12T10:00:00");
        LikeMetricEventCriteria likeEvent =
                new LikeMetricEventCriteria(eventMessage, 1L, 100L, "cacheKey:100", LikeEventType.CREATED);

        when(eventHandlerService.tryConsume(eq("event1"), eq("metrics"))).thenReturn(true);

        metricsAggregationFacade.aggregateLikes(List.of(likeEvent));

        verify(productMetricDailyService).updateLike(100L, 1);
    }

    @Test
    @DisplayName("aggregateLikes: DELETED 이벤트 → like 수 감소")
    void aggregateLikes_deleted() {
        EventMessage eventMessage = new EventMessage("event2", "2025-09-12T10:05:00");
        LikeMetricEventCriteria likeEvent =
                new LikeMetricEventCriteria(eventMessage, 2L, 200L, "cacheKey:200", LikeEventType.DELETED);

        when(eventHandlerService.tryConsume(eq("event2"), eq("metrics"))).thenReturn(true);

        metricsAggregationFacade.aggregateLikes(List.of(likeEvent));

        verify(productMetricDailyService).updateLike(200L, 1);
    }

    @Test
    @DisplayName("aggregateOrders: 주문 아이템 수량 합산 후 updateSale 호출")
    void aggregateOrders_shouldUpdateSales() {
        EventMessage eventMessage = new EventMessage("event3", "2025-09-12T11:00:00");

        OrderItem item1 = new OrderItem(10L, 2L);
        OrderItem item2 = new OrderItem(20L, 1L);
        OrderItem item3 = new OrderItem(10L, 1L);

        OrderMetricEventCriteria event =
                new OrderMetricEventCriteria(eventMessage, List.of(item1, item2, item3));

        when(eventHandlerService.tryConsume(eq("event3"), eq("metrics"))).thenReturn(true);

        metricsAggregationFacade.aggregateOrders(List.of(event));

        verify(productMetricDailyService).updateSale(10L, 2);
        verify(productMetricDailyService).updateSale(20L, 1);
    }

    @Test
    @DisplayName("aggregateViews: 조회수 집계 후 updateView 호출")
    void aggregateViews_shouldUpdateViews() {
        EventMessage e1 = new EventMessage("event4", "2025-09-12T12:00:00");
        EventMessage e2 = new EventMessage("event5", "2025-09-12T12:01:00");
        EventMessage e3 = new EventMessage("event6", "2025-09-12T12:02:00");

        ProductMetricEventCriteria view1 = new ProductMetricEventCriteria(e1, 300L);
        ProductMetricEventCriteria view2 = new ProductMetricEventCriteria(e2, 400L);
        ProductMetricEventCriteria view3 = new ProductMetricEventCriteria(e3, 300L); // productId=300 추가 조회

        when(eventHandlerService.tryConsume(anyString(), eq("metrics"))).thenReturn(true);

        metricsAggregationFacade.aggregateViews(List.of(view1, view2, view3));

        verify(productMetricDailyService).updateView(300L, 2); // view1 + view3
        verify(productMetricDailyService).updateView(400L, 1);
    }
}
