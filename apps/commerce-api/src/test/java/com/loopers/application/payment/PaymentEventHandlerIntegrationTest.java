package com.loopers.application.payment;


import com.loopers.application.order.OrderFacade;
import com.loopers.application.order.dto.OrderInfo;
import com.loopers.application.payment.dto.PaymentCriteria;
import com.loopers.domain.order.OrderService;
import com.loopers.domain.order.event.OrderCreatedEvent;
import com.loopers.domain.payment.PaymentType;
import com.loopers.domain.product.ProductService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.context.transaction.TestTransaction;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@SpringBootTest
class PaymentEventHandlerIntegrationTest {

    @Autowired
    private ApplicationEventPublisher events;

    // 외부 PG 호출 경계: 완전 Mock
    @MockitoBean
    private PaymentFacade paymentFacade;

    // 상태 확정 경계: 실제 빈을 Spy로 감시하되 DB 변경 막기 위해 doNothing 스텁
    @MockitoSpyBean
    private OrderFacade orderFacade;

    @MockitoBean
    private OrderService orderService;

    @MockitoBean
    private ProductService productService;

    @MockitoBean
    private KafkaTemplate<Object, Object> kafkaTemplate;

    private static final Long ORDER_ID = 1000L;
    private static final Long USER_ID = 77L;
    private static final long AMOUNT = 13_995L;
    private static final PaymentType TYPE = PaymentType.CARD;
    private static final String CARD_TYPE = "SAMSUNG";
    private static final String CARD_NO = "1111-2222-3333-4444";

    @BeforeEach
    void setUp() {
        // 스파이/목 초기화
        Mockito.reset(paymentFacade, orderFacade);
        // 실제 DB에 손 안 대도록 상태 확정 메서드는 no-op으로 스텁
        doNothing().when(orderFacade).completePayment(anyLong());
        doNothing().when(orderFacade).failedPayment(anyLong());
    }

    @Nested
    @DisplayName("OrderCreatedEvent 처리")
    class OrderCreated {

        @Test
        @Transactional
        @DisplayName("커밋 후에만 PaymentEventHandler가 실행되어 pay 호출 + 성공 분기 completePayment 호출")
        void created_afterCommit_runsHandler_success() {
            // pay 성공 스텁
            doNothing().when(paymentFacade).pay(any(PaymentCriteria.CreatePayment.class));


            // 트랜잭션 안에서 이벤트 발행 → AFTER_COMMIT 예약
            events.publishEvent(new OrderCreatedEvent(
                    ORDER_ID, USER_ID, AMOUNT, TYPE, CARD_TYPE, CARD_NO
            ));

            // 커밋 전: 호출 없음
            verify(paymentFacade, never()).pay(any());
            verify(orderFacade, never()).completePayment(anyLong());
            verify(orderFacade, never()).failedPayment(anyLong());

            doReturn(new OrderInfo.OrderDetail(
                    ORDER_ID, USER_ID, null, 1000L, 1000L, null, null, null, null,
                    List.of(new OrderInfo.OrderItemResponse(1L, ORDER_ID, USER_ID, null, null, 2L, null))
            )).when(orderFacade).getOrder(anyLong());


            // 커밋
            TestTransaction.flagForCommit();
            TestTransaction.end();

            // @Async 실행 대기 후 검증
            await().atMost(Duration.ofSeconds(3)).untilAsserted(() -> {
                // pay가 1회 호출됨
                ArgumentCaptor<PaymentCriteria.CreatePayment> cap =
                        ArgumentCaptor.forClass(PaymentCriteria.CreatePayment.class);
                verify(paymentFacade, times(1)).pay(cap.capture());

                // ✨ var 대신 명시 타입
                PaymentCriteria.CreatePayment sent = cap.getValue();
                assertThat(sent.orderId()).isEqualTo(ORDER_ID);
                assertThat(sent.userId()).isEqualTo(USER_ID);
                assertThat(sent.amount()).isEqualTo(AMOUNT);
                assertThat(sent.type()).isEqualTo(TYPE);
                assertThat(sent.cardType()).isEqualTo(CARD_TYPE);
                assertThat(sent.cardNo()).isEqualTo(CARD_NO);

                verify(orderFacade, times(1)).completePayment(ORDER_ID);
                verify(orderFacade, never()).failedPayment(anyLong());
            });
        }
        @Test
        @Transactional
        @DisplayName("커밋 후 pay에서 예외가 나면 failedPayment가 호출된다")
        void afterCommit_fail_callsFailed() {
            // pay가 실패하도록 스텁
            doThrow(new RuntimeException("PG down")).when(paymentFacade).pay(any());


            events.publishEvent(new OrderCreatedEvent(
                    ORDER_ID, USER_ID, AMOUNT, TYPE, CARD_TYPE, CARD_NO
            ));

            doReturn(new OrderInfo.OrderDetail(
                    ORDER_ID, USER_ID, null, 1000L, 1000L, null, null, null, null,
                    List.of(new OrderInfo.OrderItemResponse(1L, ORDER_ID, USER_ID, null, null, 2L, null))
            )).when(orderFacade).getOrder(anyLong());


            // 커밋 전: 호출 없음
            verify(paymentFacade, never()).pay(any());
            verify(orderFacade, never()).completePayment(anyLong());
            verify(orderFacade, never()).failedPayment(anyLong());

            // 커밋
            TestTransaction.flagForCommit();
            TestTransaction.end();

            // 비동기 실행 대기 후 검증
            await().atMost(Duration.ofSeconds(3)).untilAsserted(() -> {
                verify(paymentFacade, times(1)).pay(any());
                verify(orderFacade, times(1)).failedPayment(ORDER_ID);
                verify(orderFacade, never()).completePayment(anyLong());
            });
        }

        @Test
        @Transactional
        @DisplayName("트랜잭션이 롤백되면 핸들러가 실행되지 않는다")
        void rollback_doesNotInvokeHandler() {
            events.publishEvent(new OrderCreatedEvent(
                    ORDER_ID, USER_ID, AMOUNT, TYPE, CARD_TYPE, CARD_NO
            ));

            // 커밋하지 않고 롤백
            TestTransaction.end();

            // 잠깐 대기해도 어떤 호출도 없어야 함
            await().during(Duration.ofMillis(300)).atMost(Duration.ofSeconds(1)).untilAsserted(() -> {
                verify(paymentFacade, never()).pay(any());
                verify(orderFacade, never()).completePayment(anyLong());
                verify(orderFacade, never()).failedPayment(anyLong());
            });
        }
    }
}
