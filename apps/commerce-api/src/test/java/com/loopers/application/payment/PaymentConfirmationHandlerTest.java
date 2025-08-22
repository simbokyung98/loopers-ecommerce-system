package com.loopers.application.payment;


import com.loopers.application.payment.dto.PaymentProbe;
import com.loopers.application.payment.dto.ScheduledPayment;
import com.loopers.application.payment.scheduler.PaymentConfirmationHandler;
import com.loopers.application.payment.scheduler.PaymentFollowUpScheduler;
import com.loopers.application.purchase.PurchaseFacade;
import com.loopers.domain.payment.PaymentService;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentConfirmationHandlerTest {

    @Mock PaymentGatewayService paymentGatewayService;
    @Mock PaymentService paymentService;
    @Mock PurchaseFacade purchaseFacade;
    @Mock
    PaymentFollowUpScheduler scheduler;

    @InjectMocks
    PaymentConfirmationHandler sut;

    // ---------- Tick tests ----------

    @Test
    @DisplayName("Tick: PG가 CONFIRMED면 결제완료 및 스케줄 취소")
    void tick_confirms_then_complete_and_cancel() {
        Long orderId = 101L;
        Long paymentId = 201L;
        String txKey = "tx-OK";

        when(paymentGatewayService.checkPayment(txKey))
                .thenReturn(new PaymentProbe(PaymentProbe.Decision.CONFIRMED));

        sut.onCardPendingTick(orderId, paymentId, txKey);

        verify(paymentGatewayService).checkPayment(txKey);
        verify(paymentService).completePay(paymentId);
        verify(scheduler).cancel(orderId);
        verifyNoMoreInteractions(paymentService, purchaseFacade, scheduler);
    }

    @Test
    @DisplayName("Tick: PG가 STOP이면 보상실행 및 스케줄 취소")
    void tick_stop_then_compensate_and_cancel() {
        Long orderId = 102L;
        Long paymentId = 202L;
        String txKey = "tx-STOP";

        when(paymentGatewayService.checkPayment(txKey))
                .thenReturn(new PaymentProbe(PaymentProbe.Decision.STOP));

        sut.onCardPendingTick(orderId, paymentId, txKey);

        verify(paymentGatewayService).checkPayment(txKey);
        verify(purchaseFacade).failedPayment(paymentId, orderId);
        verify(scheduler).cancel(orderId);
        verifyNoMoreInteractions(paymentService, purchaseFacade, scheduler);
    }

    @Test
    @DisplayName("Tick: PG가 RETRY이면 아무것도 하지 않음(취소/완료/보상 호출 없음)")
    void tick_retry_do_nothing() {
        Long orderId = 103L;
        Long paymentId = 203L;
        String txKey = "tx-R";

        when(paymentGatewayService.checkPayment(txKey))
                .thenReturn(new PaymentProbe(PaymentProbe.Decision.RETRY));

        sut.onCardPendingTick(orderId, paymentId, txKey);

        verify(paymentGatewayService).checkPayment(txKey);
        verifyNoInteractions(paymentService, purchaseFacade);
        verify(scheduler, never()).cancel(anyLong());
    }

    // ---------- Callback tests ----------

    @Test
    @DisplayName("Callback: 스케줄 없음이면 BAD_REQUEST 예외")
    void callback_noSchedule_then_badRequest() {
        Long orderId = 201L;
        String txKey = "tx-CB";

        when(scheduler.findScheduled(orderId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> sut.onCardPaymentCallback(orderId, txKey))
                .isInstanceOf(CoreException.class)
                .satisfies(e -> {
                    CoreException ce = (CoreException) e;
                    assert ce.getErrorType() == ErrorType.BAD_REQUEST;
                });

        verify(scheduler).findScheduled(orderId);
        verifyNoInteractions(paymentGatewayService, paymentService, purchaseFacade);
        verify(scheduler, never()).cancel(anyLong());
    }

    @Test
    @DisplayName("Callback: txKey 불일치면 BAD_REQUEST 예외")
    void callback_txKeyMismatch_then_badRequest() {
        Long orderId = 202L;
        String incoming = "tx-INCOMING";
        String scheduledTx = "tx-SCHEDULED";

        // ScheduledPayment은 record/final일 수 있으니 mock + doReturn 사용
        ScheduledPayment scheduled = mock(ScheduledPayment.class);
        when(scheduler.findScheduled(orderId)).thenReturn(Optional.of(scheduled));


        doReturn(scheduledTx).when(scheduled).txKey();

        assertThatThrownBy(() -> sut.onCardPaymentCallback(orderId, incoming))
                .isInstanceOf(CoreException.class)
                .satisfies(e -> {
                    CoreException ce = (CoreException) e;
                    assert ce.getErrorType() == ErrorType.BAD_REQUEST;
                });

        verify(scheduler).findScheduled(orderId);
        verifyNoInteractions(paymentGatewayService, paymentService, purchaseFacade);
        verify(scheduler, never()).cancel(anyLong());
    }

    @Test
    @DisplayName("Callback: txKey 일치 + CONFIRMED이면 completePay 및 스케줄 취소")
    void callback_confirmed_then_complete_and_cancel() {
        Long orderId = 203L;
        Long paymentId = 303L;
        String txKey = "tx-CF";

        ScheduledPayment scheduled = mock(ScheduledPayment.class);
        when(scheduler.findScheduled(orderId)).thenReturn(Optional.of(scheduled));
        doReturn(paymentId).when(scheduled).paymentId();
        doReturn(txKey).when(scheduled).txKey();

        when(paymentGatewayService.checkPayment(txKey))
                .thenReturn(new PaymentProbe(PaymentProbe.Decision.CONFIRMED));

        sut.onCardPaymentCallback(orderId, txKey);

        verify(scheduler).findScheduled(orderId);
        verify(paymentGatewayService).checkPayment(txKey);
        verify(paymentService).completePay(paymentId);
        verify(scheduler).cancel(orderId);
        verifyNoMoreInteractions(paymentService, purchaseFacade, scheduler);
    }

    @Test
    @DisplayName("Callback: txKey 일치 + STOP이면 보상실행 및 스케줄 취소")
    void callback_stop_then_compensate_and_cancel() {
        Long orderId = 204L;
        Long paymentId = 304L;
        String txKey = "tx-ST";

        ScheduledPayment scheduled = mock(ScheduledPayment.class);
        when(scheduler.findScheduled(orderId)).thenReturn(Optional.of(scheduled));
        doReturn(paymentId).when(scheduled).paymentId();
        doReturn(txKey).when(scheduled).txKey();

        when(paymentGatewayService.checkPayment(txKey))
                .thenReturn(new PaymentProbe(PaymentProbe.Decision.STOP));

        sut.onCardPaymentCallback(orderId, txKey);

        verify(scheduler).findScheduled(orderId);
        verify(paymentGatewayService).checkPayment(txKey);
        verify(purchaseFacade).failedPayment(paymentId, orderId);
        verify(scheduler).cancel(orderId);
        verifyNoMoreInteractions(paymentService, purchaseFacade, scheduler);
    }

    @Test
    @DisplayName("Callback: txKey 일치 + RETRY이면 아무것도 하지 않음(취소/완료/보상 호출 없음)")
    void callback_retry_do_nothing() {
        Long orderId = 205L;
        Long paymentId = 305L;
        String txKey = "tx-RR";

        ScheduledPayment scheduled = mock(ScheduledPayment.class);
        when(scheduler.findScheduled(orderId)).thenReturn(Optional.of(scheduled));
        doReturn(paymentId).when(scheduled).paymentId();
        doReturn(txKey).when(scheduled).txKey();

        when(paymentGatewayService.checkPayment(txKey))
                .thenReturn(new PaymentProbe(PaymentProbe.Decision.RETRY));

        sut.onCardPaymentCallback(orderId, txKey);

        verify(scheduler).findScheduled(orderId);
        verify(paymentGatewayService).checkPayment(txKey);
        verifyNoInteractions(paymentService, purchaseFacade);
        verify(scheduler, never()).cancel(anyLong());
    }
}
