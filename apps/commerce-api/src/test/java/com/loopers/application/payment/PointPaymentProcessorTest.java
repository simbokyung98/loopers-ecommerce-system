package com.loopers.application.payment;


import com.loopers.application.payment.dto.PaymentCriteria;
import com.loopers.application.payment.processor.PointPaymentProcessor;
import com.loopers.domain.payment.PaymentCommand;
import com.loopers.domain.payment.PaymentService;
import com.loopers.domain.payment.PaymentStatus;
import com.loopers.domain.payment.PaymentType;
import com.loopers.domain.point.PointService;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PointPaymentProcessorTest {

    @Mock
    PointService pointService;

    @Mock
    PaymentService paymentService;

    @InjectMocks
    PointPaymentProcessor sut;

    @Test
    @DisplayName("supports: POINT 타입을 지원한다")
    void supports_point() {
        assertThat(sut.supports()).isEqualTo(PaymentType.POINT);
    }

    @Test
    @DisplayName("정상: 포인트 차감 성공 시 SUCCEEDED 저장, 예외 없음")
    void process_success_savesSucceeded() {
        // given
        PaymentCriteria.CreatePayment criteria = mock(PaymentCriteria.CreatePayment.class);
        when(criteria.userId()).thenReturn(10L);
        when(criteria.amount()).thenReturn(500L);

        PaymentCommand.CreatePointPayment cmdSucceeded = mock(PaymentCommand.CreatePointPayment.class);
        when(criteria.toPointCommand(eq(PaymentStatus.SUCCEEDED))).thenReturn(cmdSucceeded);

        when(paymentService.payPoint(cmdSucceeded)).thenReturn(null);

        // when
        assertThatCode(() -> sut.process(criteria)).doesNotThrowAnyException();

        // then
        verify(pointService, times(1)).spend(10L, 500L);
        verify(criteria, times(1)).toPointCommand(PaymentStatus.SUCCEEDED);
        verify(paymentService, times(1)).payPoint(cmdSucceeded);
        verify(criteria, never()).toPointCommand(PaymentStatus.FAILED);
    }

    @Test
    @DisplayName("부족: IllegalStateException이면 FAILED 저장 후 CoreException(BAD_REQUEST) 던짐")
    void process_insufficient_savesFailed_thenThrowsCoreBadRequest() {
        // given
        PaymentCriteria.CreatePayment criteria = mock(PaymentCriteria.CreatePayment.class);
        when(criteria.userId()).thenReturn(20L);
        when(criteria.amount()).thenReturn(10_000L);

        // 포인트 부족
        doThrow(new IllegalStateException("포인트가 부족합니다."))
                .when(pointService).spend(20L, 10_000L);

        PaymentCommand.CreatePointPayment cmdFailed = mock(PaymentCommand.CreatePointPayment.class);
        when(criteria.toPointCommand(eq(PaymentStatus.FAILED))).thenReturn(cmdFailed);
        when(paymentService.payPoint(cmdFailed)).thenReturn(null);

        // when / then
        assertThatThrownBy(() -> sut.process(criteria))
                .isInstanceOf(CoreException.class)
                .satisfies(e -> assertThat(((CoreException) e).getErrorType())
                        .isEqualTo(ErrorType.BAD_REQUEST));

        // 실패 저장이 호출되었는지
        verify(criteria, times(1)).toPointCommand(PaymentStatus.FAILED);
        verify(paymentService, times(1)).payPoint(cmdFailed);

        // 성공 저장은 호출되지 않음
        verify(criteria, never()).toPointCommand(PaymentStatus.SUCCEEDED);
        verify(paymentService, never()).payPoint(argThat(arg -> arg != cmdFailed));
    }

    @Test
    @DisplayName("기타 예외: IllegalArgumentException이면 저장 없이 예외 전파")
    void process_otherException_isPropagated_withoutSave() {

        PaymentCriteria.CreatePayment criteria = mock(PaymentCriteria.CreatePayment.class);
        when(criteria.userId()).thenReturn(30L);
        when(criteria.amount()).thenReturn(0L);

        // 잘못된 입력 등
        doThrow(new CoreException(ErrorType.BAD_REQUEST,"사용금액은 0 보다 커야합니다."))
                .when(pointService).spend(30L, 0L);


        assertThatThrownBy(() -> sut.process(criteria))
                .isInstanceOf(CoreException.class);

        // 저장 로직은 어떤 것도 호출되지 않아야 함
        verify(paymentService, never()).payPoint(any());
        verify(criteria, never()).toPointCommand(any());
    }
}
