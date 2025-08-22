package com.loopers.application.payment;


import com.loopers.application.payment.dto.PaymentCriteria;
import com.loopers.application.payment.processor.CardPaymentProcessor;
import com.loopers.application.payment.scheduler.PaymentFollowUpScheduler;
import com.loopers.domain.payment.PaymentCommand;
import com.loopers.domain.payment.PaymentModel;
import com.loopers.domain.payment.PaymentService;
import com.loopers.domain.payment.PaymentType;
import com.loopers.infrastructure.http.dto.PaymentCreateRequest;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CardPaymentProcessorTest {

    @Mock
    PaymentService paymentService;

    @Mock
    PaymentGatewayService paymentGatewayService;

    @Mock
    PaymentFollowUpScheduler followUpScheduler;

    @InjectMocks
    CardPaymentProcessor sut;

    @Captor
    ArgumentCaptor<PaymentCommand.CreateCardPayment> commandCaptor;

    @Test
    @DisplayName("성공: PG txKey 발급 → payCardPending 저장 후 스케줄러 등록")
    void process_success_registersScheduler() {
        // given
        Long orderId = 123L;
        Long savedPaymentId = 777L;
        String txKey = "TX-123";

        PaymentCriteria.CreatePayment criteria = mock(PaymentCriteria.CreatePayment.class);
        when(criteria.orderId()).thenReturn(orderId); // ✅ 스케줄러에서 사용됨
        PaymentCreateRequest req = new PaymentCreateRequest("ORD-123", "SAMSUNG", "1111-2222-3333-4444", "10000");
        when(criteria.toRequest()).thenReturn(req);

        when(paymentGatewayService.requestWithStatusRecovery(eq(req))).thenReturn(txKey);

        PaymentCommand.CreateCardPayment cmd = mock(PaymentCommand.CreateCardPayment.class);
        when(criteria.toCommand(eq(txKey))).thenReturn(cmd);

        PaymentModel model = mock(PaymentModel.class);
        when(model.getId()).thenReturn(savedPaymentId);
        when(paymentService.payCardPending(any())).thenReturn(model);

        //act
        sut.process(criteria);

        // assert
        verify(paymentGatewayService, times(1)).requestWithStatusRecovery(eq(req));
        verify(criteria, times(1)).toCommand(eq(txKey));
        verify(paymentService, times(1)).payCardPending(commandCaptor.capture());
        assertThat(commandCaptor.getValue()).isNotNull();

        verify(followUpScheduler, times(1))
                .scheduleEveryMinute(eq(orderId), eq(savedPaymentId), eq(txKey), eq(30));
    }

    @Test
    @DisplayName("방어로직: txKey가 null이면 INTERNAL_ERROR 예외, 저장/스케줄러 미호출")
    void process_throwsInternalError_whenTxKeyNull() {
        // given
        PaymentCriteria.CreatePayment criteria = mock(PaymentCriteria.CreatePayment.class);
        PaymentCreateRequest req = new PaymentCreateRequest("ORD-55", "SAMSUNG", "1111-2222-3333-4444", "10000");
        when(criteria.toRequest()).thenReturn(req);

        when(paymentGatewayService.requestWithStatusRecovery(eq(req))).thenReturn(null); // ⬅️ 여기서 종료

        // assert
        assertThatThrownBy(() -> sut.process(criteria))
                .isInstanceOf(CoreException.class)
                .satisfies(e -> assertThat(((CoreException) e).getErrorType())
                        .isEqualTo(ErrorType.INTERNAL_ERROR));

        verify(paymentService, never()).payCardPending(any());
        verify(followUpScheduler, never()).scheduleEveryMinute(anyLong(), anyLong(), anyString(), anyInt());
        verify(criteria, never()).toCommand(anyString());
        verify(criteria, never()).orderId(); // ✅ 호출 안 됨
    }

    @Test
    @DisplayName("방어로직: txKey가 공백이면 INTERNAL_ERROR 예외, 저장/스케줄러 미호출")
    void process_throwsInternalError_whenTxKeyBlank() {
        // given
        PaymentCriteria.CreatePayment criteria = mock(PaymentCriteria.CreatePayment.class);
        PaymentCreateRequest req = new PaymentCreateRequest("ORD-77", "SAMSUNG", "1111-2222-3333-4444", "10000");
        when(criteria.toRequest()).thenReturn(req);

        when(paymentGatewayService.requestWithStatusRecovery(eq(req))).thenReturn("   "); // ⬅️ 여기서 종료

        // assert
        assertThatThrownBy(() -> sut.process(criteria))
                .isInstanceOf(CoreException.class)
                .satisfies(e -> assertThat(((CoreException) e).getErrorType())
                        .isEqualTo(ErrorType.INTERNAL_ERROR));

        verify(paymentService, never()).payCardPending(any());
        verify(followUpScheduler, never()).scheduleEveryMinute(anyLong(), anyLong(), anyString(), anyInt());
        verify(criteria, never()).toCommand(anyString());
        verify(criteria, never()).orderId(); // ✅ 호출 안 됨
    }

    @Test
    @DisplayName("PG 4xx(CoreException BAD_REQUEST) 발생 시 예외 전파, 저장/스케줄러 미호출")
    void process_propagatesBadRequest_whenPgDeclined() {
        // given
        PaymentCriteria.CreatePayment criteria = mock(PaymentCriteria.CreatePayment.class);
        PaymentCreateRequest req = new PaymentCreateRequest("ORD-400", "SAMSU", "1111-2222-3333-4444", "10000"); // 잘못된 카드타입
        when(criteria.toRequest()).thenReturn(req);

        when(paymentGatewayService.requestWithStatusRecovery(eq(req)))
                .thenThrow(new CoreException(ErrorType.BAD_REQUEST, "필드 'cardType' 오류"));

        // assert
        assertThatThrownBy(() -> sut.process(criteria))
                .isInstanceOf(CoreException.class)
                .satisfies(e -> assertThat(((CoreException) e).getErrorType())
                        .isEqualTo(ErrorType.BAD_REQUEST));

        verify(paymentService, never()).payCardPending(any());
        verify(followUpScheduler, never()).scheduleEveryMinute(anyLong(), anyLong(), anyString(), anyInt());
        verify(criteria, never()).toCommand(anyString());
        verify(criteria, never()).orderId();
    }

    @Test
    @DisplayName("도메인 저장(payCardPending)에서 실패하면 예외 전파, 스케줄러 미호출")
    void process_propagates_whenDomainSaveFails() {

        String txKey = "TX-333";
        PaymentCriteria.CreatePayment criteria = mock(PaymentCriteria.CreatePayment.class);
        PaymentCreateRequest req = new PaymentCreateRequest("ORD-333", "SAMSUNG", "1111-2222-3333-4444", "10000");
        when(criteria.toRequest()).thenReturn(req);

        when(paymentGatewayService.requestWithStatusRecovery(eq(req))).thenReturn(txKey);

        PaymentCommand.CreateCardPayment cmd = mock(PaymentCommand.CreateCardPayment.class);
        when(criteria.toCommand(eq(txKey))).thenReturn(cmd);

        when(paymentService.payCardPending(eq(cmd)))
                .thenThrow(new CoreException(ErrorType.INTERNAL_ERROR, "저장 실패"));

        // assert
        assertThatThrownBy(() -> sut.process(criteria))
                .isInstanceOf(CoreException.class)
                .satisfies(e -> assertThat(((CoreException) e).getErrorType())
                        .isEqualTo(ErrorType.INTERNAL_ERROR));

        verify(followUpScheduler, never()).scheduleEveryMinute(anyLong(), anyLong(), anyString(), anyInt());
        verify(criteria, never()).orderId(); // ✅ 스케줄까지 안 가므로 호출 안 됨
    }

    @Test
    @DisplayName("supports()는 CARD 타입을 반환한다")
    void supports_returnsCard() {
        assertThat(sut.supports()).isEqualTo(PaymentType.CARD);
    }
}
