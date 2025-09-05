package com.loopers.interfaces.consumer;

import com.loopers.confg.kafka.KafkaMessage;
import com.loopers.domain.metric.ProductMetricDailyRepository;
import com.loopers.domain.metric.ProductMetricDailyService;
import com.loopers.interfaces.consumer.event.*;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.annotation.DirtiesContext;

import java.time.Duration;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@DirtiesContext // 매 테스트마다 컨텍스트 초기화 (토픽 충돌 방지)
@EmbeddedKafka(partitions = 1, topics = {
        "product.viewed.v1",
        "payment.confirmed.v1",
        "like.v1"
})
class MetricsConsumerIntegrationTest {

    @Autowired
    private KafkaTemplate<Object, Object> kafkaTemplate;

    @Autowired
    private ProductMetricDailyService productMetricDailyService;

    @Autowired
    private ProductMetricDailyRepository repository;

    @Test
    @DisplayName("상품 조회 이벤트 여러 건 발행 시: MetricsConsumer 가 배치로 viewCount 업데이트 한다")
    void consumeProductViewedEvent_shouldUpdateViewCount() {
        Long productId = 100L;

        // when: 메시지 여러 건 발행 (배치 소비 검증용)
        for (int i = 0; i < 5; i++) {
            ProductViewedEvent event = new ProductViewedEvent(productId);
            KafkaMessage<ProductViewedEvent> message = KafkaMessage.from(event);
            kafkaTemplate.send("product.viewed.v1", productId.toString(), message);
        }

        // then: Awaitility로 배치 결과 검증
        Awaitility.await()
                .atMost(Duration.ofSeconds(10))
                .untilAsserted(() -> {
                    var dailyOpt = repository.findByProductIdAndDate(productId, LocalDate.now());
                    assertThat(dailyOpt).isPresent();
                    assertThat(dailyOpt.get().getViewCount()).isEqualTo(5);
                });
    }

    @Test
    @DisplayName("주문 완료 이벤트 발행 시: MetricsConsumer 가 saleCount 업데이트 한다")
    void consumeOrderCreated_shouldUpdateSaleCount() {
        Long productId = 200L;
        ConfirmedOrderItem item1 = new ConfirmedOrderItem(productId, 2L);
        ConfirmedOrderItem item2 = new ConfirmedOrderItem(productId, 3L);

        PaymentConfirmedEvent event = new PaymentConfirmedEvent(1L, 2L, 3L, List.of(item1, item2));
        KafkaMessage<PaymentConfirmedEvent> message = KafkaMessage.from(event);

        kafkaTemplate.send("product.payment.confirmed.v1", "order-1", message);

        Awaitility.await()
                .atMost(Duration.ofSeconds(10))
                .untilAsserted(() -> {
                    var daily = repository.findByProductIdAndDate(productId, LocalDate.now());
                    assertThat(daily).isPresent();
                    assertThat(daily.get().getSaleCount()).isEqualTo(5); // 2 + 3
                });
    }
    
}
