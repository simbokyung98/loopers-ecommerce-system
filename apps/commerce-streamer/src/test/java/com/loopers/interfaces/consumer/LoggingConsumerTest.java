package com.loopers.interfaces.consumer;

import com.loopers.confg.kafka.KafkaMessage;
import com.loopers.domain.event.EventHandlerService;
import com.loopers.domain.log.AuditEventType;
import com.loopers.domain.log.AuditLogCommand;
import com.loopers.domain.log.AuditLogService;
import com.loopers.interfaces.consumer.LoggingConsumer;
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
class LoggingConsumerTest {

    @Mock
    private EventHandlerService eventHandlerService;

    @Mock
    private AuditLogService auditLogService;

    @InjectMocks
    private LoggingConsumer consumer;

    @Test
    @DisplayName("좋아요 이벤트 발생 시: AuditLog 저장")
    void onLikeEvents_shouldSaveAuditLog() {
        // given
        LikeEvent payload = new LikeEvent(1L, 200L, null, LikeEventType.CREATED);
        KafkaMessage<LikeEvent> message = KafkaMessage.from(payload);
        when(eventHandlerService.tryConsume(any(), eq("logging"))).thenReturn(true);

        // when
        consumer.onLikeEvents(List.of(message), mock(Acknowledgment.class));

        // then
        verify(auditLogService).save(
                new AuditLogCommand.AuditLog(
                        message.eventId(),
                        AuditEventType.LIKE_CREATED,
                        payload.userId(),
                        payload.productId()
                )
        );
    }

    @Test
    @DisplayName("주문 생성 이벤트 발생 시: AuditLog 저장")
    void onOrderCreateEvent_shouldSaveAuditLog() {
        // given
        OrderCreatedKafkaEvent payload = new OrderCreatedKafkaEvent(1L, 500L, null);
        KafkaMessage<OrderCreatedKafkaEvent> message = KafkaMessage.from(payload);
        when(eventHandlerService.tryConsume(any(), eq("logging"))).thenReturn(true);

        // when
        consumer.onOrderCreateEvent(List.of(message), mock(Acknowledgment.class));

        // then
        verify(auditLogService).save(
                new AuditLogCommand.AuditLog(
                        message.eventId(),
                        AuditEventType.ORDER_CREATED,
                        payload.userId(),
                        payload.orderId()
                )
        );
    }

    @Test
    @DisplayName("결제 실패 이벤트 발생 시: AuditLog 저장")
    void onPaymentFailedEvent_shouldSaveAuditLog() {
        // given
        PaymentFailedEvent payload = new PaymentFailedEvent(1L, 900L, 2L);
        KafkaMessage<PaymentFailedEvent> message = KafkaMessage.from(payload);
        when(eventHandlerService.tryConsume(any(), eq("logging"))).thenReturn(true);

        // when
        consumer.onPaymentFailedEvent(List.of(message), mock(Acknowledgment.class));

        // then
        verify(auditLogService).save(
                new AuditLogCommand.AuditLog(
                        message.eventId(),
                        AuditEventType.PAYMENT_FAILED,
                        payload.userId(),
                        payload.paymentId()
                )
        );
    }

    @Test
    @DisplayName("결제 성공 이벤트 발생 시: AuditLog 저장")
    void onPaymentConfirmedEvent_shouldSaveAuditLog() {
        // given
        List<ConfirmedOrderItem> items = List.of(new ConfirmedOrderItem(100L, 2L));
        PaymentConfirmedEvent payload = new PaymentConfirmedEvent(1L, 901L, 2L, items);
        KafkaMessage<PaymentConfirmedEvent> message = KafkaMessage.from(payload);
        when(eventHandlerService.tryConsume(any(), eq("logging"))).thenReturn(true);

        // when
        consumer.onPaymentConfirmedEvent(List.of(message), mock(Acknowledgment.class));

        // then
        verify(auditLogService).save(
                new AuditLogCommand.AuditLog(
                        message.eventId(),
                        AuditEventType.PAYMENT_SUCCESS,
                        payload.userId(),
                        payload.paymentId()
                )
        );
    }
}
