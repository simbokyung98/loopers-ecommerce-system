package com.loopers.application.metric;


import com.loopers.application.metric.dto.LikeMetricEventCriteria;
import com.loopers.application.metric.dto.OrderItem;
import com.loopers.application.metric.dto.OrderMetricEventCriteria;
import com.loopers.application.metric.dto.ProductMetricEventCriteria;
import com.loopers.domain.event.EventHandlerService;
import com.loopers.domain.metric.ProductMetricDailyService;
import com.loopers.interfaces.consumer.event.LikeEventType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Component
@RequiredArgsConstructor
public class MetricsAggregationFacade {

    private final EventHandlerService eventHandlerService;
    private final ProductMetricDailyService productMetricDailyService;

    @Transactional
    public void aggregateLikes(List<LikeMetricEventCriteria> criteriaList){

        List<LikeMetricEventCriteria> accepted = criteriaList.stream()
                .filter(c -> eventHandlerService.tryConsume(c.eventMessage().eventId(), "metrics"))
                .toList();

        Map<Long, Long> created = accepted.stream()
                .filter(e -> e.type() == LikeEventType.CREATED)
                .collect(Collectors.groupingBy(
                        LikeMetricEventCriteria::productId,
                        Collectors.counting()
                ));

        Map<Long, Long> deleted = accepted.stream()
                .filter(e -> e.type() == LikeEventType.DELETED)
                .collect(Collectors.groupingBy(
                        LikeMetricEventCriteria::productId,
                        Collectors.counting()
                ));

        created.forEach((pid, cnt) -> productMetricDailyService.updateLike(pid, cnt.intValue()));
        deleted.forEach((pid, cnt) -> productMetricDailyService.updateLike(pid, cnt.intValue()));

    }

    @Transactional
    public void aggregateOrders(List<OrderMetricEventCriteria> criteriaList){
        List<OrderMetricEventCriteria> accepted = criteriaList.stream()
                .filter(c -> eventHandlerService.tryConsume(c.eventMessage().eventId(), "metrics"))
                .toList();


        // 상품별 판매 수량 합산
        Map<Long, Long> saleCounts = accepted.stream()
                .flatMap(e -> e.orderItemList().stream())
                .collect(Collectors.groupingBy(
                        OrderItem::productId,
                        Collectors.counting()
                ));

        saleCounts.forEach((productId, count) ->
                productMetricDailyService.updateSale(productId, count.intValue()));
    }

    @Transactional
    public void aggregateViews(List<ProductMetricEventCriteria> criteriaList){
        Map<Long, Long> viewCounts = criteriaList.stream()
                .filter(e -> eventHandlerService.tryConsume(e.eventMessage().eventId(), "metrics"))
                .collect(Collectors.groupingBy(
                        ProductMetricEventCriteria::productId,
                        Collectors.counting()
                ));

        // 상품별 증가량 반영
        viewCounts.forEach((productId, count) ->
                productMetricDailyService.updateView(productId, count.intValue())
        );

    }
}
