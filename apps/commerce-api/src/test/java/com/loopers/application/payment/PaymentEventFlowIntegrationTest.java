package com.loopers.application.payment;


import com.loopers.application.order.OrderFacade;
import com.loopers.application.order.dto.OrderCriteria;
import com.loopers.application.order.dto.OrderInfo;
import com.loopers.application.payment.dto.PaymentCriteria;
import com.loopers.domain.order.event.OrderCreatedEvent;
import com.loopers.domain.payment.PaymentType;
import com.loopers.domain.payment.event.PaymentFailedEvent;
import com.loopers.domain.product.ProductModel;
import com.loopers.domain.product.ProductRepository;
import com.loopers.domain.product.ProductStatus;
import com.loopers.domain.user.UserModel;
import com.loopers.domain.user.UserRepository;
import com.loopers.interfaces.api.User.Gender;
import com.loopers.utils.DatabaseCleanUp;
import com.loopers.utils.RedisCleanUp;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.context.event.ApplicationEvents;
import org.springframework.test.context.event.RecordApplicationEvents;
import org.springframework.test.context.transaction.TestTransaction;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@SpringBootTest
@RecordApplicationEvents
class PaymentEventFlowIntegrationTest {

    @Autowired
    private OrderFacade orderFacade;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private DatabaseCleanUp databaseCleanUp;
    @Autowired
    private RedisCleanUp redisCleanUp;

    @MockitoBean
    private PaymentFacade paymentFacade;

    @MockitoSpyBean
    private OrderFacade spyOrderFacade;

    private static final Long BRAND_ID = 1L;

    @AfterEach
    void tearDown() {
        databaseCleanUp.truncateAllTables();
        redisCleanUp.truncateAll();
    }

    private OrderCriteria.Order newRequest(Long userId, List<OrderCriteria.ProductQuantity> items) {
        return new OrderCriteria.Order(
                userId,
                null,                         // 쿠폰 없음 (단순 경로)
                "서울시 어딘가",
                "01011112222",
                "홍길동",
                PaymentType.CARD,
                "SAMSUNG",
                "1111-2222-3333-4444",
                items
        );
    }

    @Test
    @Transactional
    @DisplayName("파사드가 이벤트 발행 → 커밋 후 리스너가 pay 호출 → completePayment 분기")
    void publishAndConsume_success(ApplicationEvents events) {
        // given
        UserModel user = userRepository.save(new UserModel("u1", Gender.MALE.getCode(), "1990-01-01", "u1@test.com"));
        ProductModel p1 = productRepository.saveProduct(new ProductModel("A", 5L, 5_000L, ProductStatus.SELL, BRAND_ID));
        ProductModel p2 = productRepository.saveProduct(new ProductModel("B", 10L, 1_000L, ProductStatus.SELL, BRAND_ID));
        OrderCriteria.Order req = newRequest(user.getId(), List.of(
                new OrderCriteria.ProductQuantity(p1.getId(), 2L),
                new OrderCriteria.ProductQuantity(p2.getId(), 4L)
        ));
        long expectedAmount = 14_000L;

        // when
        OrderInfo.OrderResponse res = orderFacade.order(req);

        // 커밋 전:
        verify(paymentFacade, never()).pay(any(PaymentCriteria.CreatePayment.class));
        verify(spyOrderFacade, never()).completePayment(anyLong());
        verify(spyOrderFacade, never()).failedPayment(anyLong());

        // 발행된 이벤트 페이로드 확인
        OrderCreatedEvent published = events.stream(OrderCreatedEvent.class)
                .findFirst().orElseThrow(() -> new AssertionError("OrderCreatedEvent not published"));
        assertThat(published.orderId()).isEqualTo(res.orderId());
        assertThat(published.amount()).isEqualTo(expectedAmount);

        // 커밋
        TestTransaction.flagForCommit();
        TestTransaction.end();

        // then
        await().atMost(Duration.ofSeconds(3)).untilAsserted(() -> {
            ArgumentCaptor<PaymentCriteria.CreatePayment> cap =
                    ArgumentCaptor.forClass(PaymentCriteria.CreatePayment.class);
            verify(paymentFacade, times(1)).pay(cap.capture());

            PaymentCriteria.CreatePayment sent = cap.getValue();
            assertThat(sent.orderId()).isEqualTo(res.orderId());
            assertThat(sent.userId()).isEqualTo(user.getId());
            assertThat(sent.amount()).isEqualTo(expectedAmount);
            assertThat(sent.type()).isEqualTo(req.type());
            assertThat(sent.cardType()).isEqualTo(req.cardType());
            assertThat(sent.cardNo()).isEqualTo(req.cardNo());

            verify(spyOrderFacade, times(1)).completePayment(res.orderId());
            verify(spyOrderFacade, never()).failedPayment(anyLong());
            assertThat(events.stream(PaymentFailedEvent.class).count()).isEqualTo(0L);
        });
    }

    @Test
    @Transactional
    @DisplayName("파사드가 이벤트 발행 → 커밋 후 pay 예외 → failedPayment 분기 + 실패 이벤트 발행")
    void publishAndConsume_fail(ApplicationEvents events) {
        // given
        UserModel user = userRepository.save(new UserModel("u2", Gender.FEMALE.getCode(), "1991-01-01", "u2@test.com"));
        ProductModel p = productRepository.saveProduct(new ProductModel("P", 3L, 2_000L, ProductStatus.SELL, BRAND_ID));
        OrderCriteria.Order req = newRequest(user.getId(),
                List.of(new OrderCriteria.ProductQuantity(p.getId(), 2L))); // 4_000

        // PG 실패 스텁
        doThrow(new RuntimeException("PG down")).when(paymentFacade).pay(any(PaymentCriteria.CreatePayment.class));

        // when
        OrderInfo.OrderResponse res = orderFacade.order(req);

        // 커밋 전: 동작 없어야 함
        verify(paymentFacade, never()).pay(any());
        verify(spyOrderFacade, never()).failedPayment(anyLong());

        // 발행 확인
        assertThat(events.stream(OrderCreatedEvent.class).count()).isEqualTo(1L);

        // 커밋
        TestTransaction.flagForCommit();
        TestTransaction.end();

        // then
        await().atMost(Duration.ofSeconds(3)).untilAsserted(() -> {
            verify(paymentFacade, times(1)).pay(any());
            verify(spyOrderFacade, times(1)).failedPayment(res.orderId());
            verify(spyOrderFacade, never()).completePayment(anyLong());
            assertThat(events.stream(PaymentFailedEvent.class).count()).isGreaterThan(0L);
        });
    }

    @Test
    @Transactional
    @DisplayName("롤백: 이벤트는 발행되지만 AFTER_COMMIT 리스너는 실행되지 않는다")
    void publishButRollback_listenerNotRun(ApplicationEvents events) {
        // given
        UserModel user = userRepository.save(new UserModel("u3", Gender.MALE.getCode(), "1992-02-02", "u3@test.com"));
        ProductModel p = productRepository.saveProduct(new ProductModel("X", 1L, 10_000L, ProductStatus.SELL, BRAND_ID));
        OrderCriteria.Order req = newRequest(user.getId(),
                List.of(new OrderCriteria.ProductQuantity(p.getId(), 1L)));

        // when
        orderFacade.order(req);

        // 발행 자체는 확인 가능
        assertThat(events.stream(OrderCreatedEvent.class).count()).isEqualTo(1L);

        // 커밋하지 않고 롤백
        TestTransaction.end();

        // then: 리스너는 미실행
        await().during(Duration.ofMillis(300)).atMost(Duration.ofSeconds(1)).untilAsserted(() -> {
            verify(paymentFacade, never()).pay(any());
            verify(spyOrderFacade, never()).completePayment(anyLong());
            verify(spyOrderFacade, never()).failedPayment(anyLong());
        });
    }
}
