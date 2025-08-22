package com.loopers.application.purchase;


import com.loopers.application.order.OrderFacade;
import com.loopers.application.order.dto.OrderCriteria;
import com.loopers.application.order.dto.OrderInfo;
import com.loopers.application.payment.PaymentFacade;
import com.loopers.application.payment.dto.PaymentCriteria;
import com.loopers.domain.user.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PurchaseFacadeTest {

    @Mock
    PaymentFacade paymentFacade;
    @Mock
    OrderFacade orderFacade;
    @Mock
    UserService userService;

    @InjectMocks
    PurchaseFacade sut;

    @Test
    @DisplayName("성공 플로우: pay 성공 → completePayment 호출 후 getOrder 반환")
    void purchase_success_flow() {
        // given
        PurchaseCriteria.Purchase criteria = mock(PurchaseCriteria.Purchase.class);

        when(criteria.userId()).thenReturn(10L);
        OrderCriteria.Order orderRequest = mock(OrderCriteria.Order.class);
        when(criteria.toOrder()).thenReturn(orderRequest);

        // 최초 주문 생성 응답
        OrderInfo.OrderResponse created = mock(OrderInfo.OrderResponse.class);
        when(created.orderId()).thenReturn(100L);
        when(created.finalCount()).thenReturn(2L);
        when(orderFacade.order(orderRequest)).thenReturn(created);

        // 결제 요청용 criteria
        PaymentCriteria.CreatePayment payCriteria = mock(PaymentCriteria.CreatePayment.class);
        when(criteria.toPayment(100L, 2L)).thenReturn(payCriteria);

        // 최종 반환용 조회 응답
        OrderInfo.OrderResponse finalView = mock(OrderInfo.OrderResponse.class);
        when(orderFacade.getOrder(100L)).thenReturn(finalView);

        // when
        OrderInfo.OrderResponse result = sut.purchase(criteria);

        // then
        verify(userService).checkExistUser(10L);
        verify(orderFacade).order(orderRequest);
        verify(paymentFacade).pay(payCriteria);
        verify(orderFacade).completePayment(100L);
        verify(orderFacade).getOrder(100L);

        assertThat(result).isSameAs(finalView);
        verify(orderFacade, never()).failPayment(anyLong());
    }

    @Test
    @DisplayName("결제 중 예외: failPayment가 호출되고 getOrder 반환(보상 동작)")
    void purchase_compensation_onPaymentFailure() {

        PurchaseCriteria.Purchase criteria = mock(PurchaseCriteria.Purchase.class);

        when(criteria.userId()).thenReturn(20L);
        OrderCriteria.Order orderRequest = mock(OrderCriteria.Order.class);
        when(criteria.toOrder()).thenReturn(orderRequest);

        OrderInfo.OrderResponse created = mock(OrderInfo.OrderResponse.class);
        when(created.orderId()).thenReturn(200L);
        when(created.finalCount()).thenReturn(3L);
        when(orderFacade.order(orderRequest)).thenReturn(created);

        PaymentCriteria.CreatePayment payCriteria = mock(PaymentCriteria.CreatePayment.class);
        when(criteria.toPayment(200L, 3L)).thenReturn(payCriteria);

        // 결제에서 예외 발생
        doThrow(new RuntimeException("pay failed"))
                .when(paymentFacade).pay(payCriteria);

        OrderInfo.OrderResponse finalView = mock(OrderInfo.OrderResponse.class);
        when(orderFacade.getOrder(200L)).thenReturn(finalView);

        // when
        OrderInfo.OrderResponse result = sut.purchase(criteria);

        // then
        verify(userService).checkExistUser(20L);
        verify(orderFacade).order(orderRequest);
        verify(paymentFacade).pay(payCriteria);

        // 보상: 결제 실패 처리
        verify(orderFacade).failPayment(200L);

        // 성공 완료는 호출되지 않음
        verify(orderFacade, never()).completePayment(anyLong());

        // 마지막 조회 반환
        verify(orderFacade).getOrder(200L);
        assertThat(result).isSameAs(finalView);
    }

}
