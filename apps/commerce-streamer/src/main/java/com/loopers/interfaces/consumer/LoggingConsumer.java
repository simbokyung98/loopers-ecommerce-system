package com.loopers.interfaces.consumer;


import com.loopers.confg.kafka.KafkaConfig;
import com.loopers.confg.kafka.KafkaMessage;
import com.loopers.domain.event.EventHandlerService;
import com.loopers.domain.log.AuditEventType;
import com.loopers.domain.log.AuditLogCommand;
import com.loopers.domain.log.AuditLogService;
import com.loopers.interfaces.consumer.event.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class LoggingConsumer {

    private final EventHandlerService eventHandlerService;
    private final AuditLogService auditLogService;

    // ============================
    // [왜 기본 배치 팩토리(BATCH_LISTENER_DEFAULT)를 쓰는가?]
    //
    // 1. 로깅은 실시간성이 중요하지 않다
    //    - 집계/캐시와 달리, "몇 초 늦더라도" 모든 이벤트를 빠짐없이 기록하는 것이 목적
    //
    // 2. 대량 처리(batch)가 유리하다
    //    - 로그성 데이터는 이벤트가 많이 발생할 수 있음
    //    - 개별 insert 보다는 배치 단위로 묶어서 DB 적재하는 것이 성능상 유리
    //
    // 3. 범용 설정이 잘 맞는다
    //    - 기본 BATCH_LISTENER_DEFAULT 는 max.poll.records=3000, fetch.min.bytes=1MB 등
    //    - 대량 메시지를 모아 처리하는 데 적합 → 로깅 목적과 잘 맞음
    //
    // 결론:
    //   * 집계/캐시는 "빠른 반영"이 필요해서 전용 Factory를 만들었지만,
    //   * 로깅은 "안정적인 배치 처리"만 보장되면 충분하므로 기본 배치 팩토리를 그대로 사용
    // ============================

    @KafkaListener(
            topics = "${demo-kafka.like.topic-name}",
            groupId = "logging-consumer-group",
            containerFactory = KafkaConfig.BATCH_LISTENER
    )
    public void onLikeEvents(List<KafkaMessage<LikeEvent>> events, Acknowledgment ack) {
        events.stream()
                .filter(e -> eventHandlerService.tryConsume(e.eventId(), "logging"))
                .forEach(message -> {
                LikeEvent likeEvent = message.payload();
                    auditLogService.save(
                            new AuditLogCommand.AuditLog(
                                    message.eventId(),
                                    likeEvent.type() == LikeEventType.CREATED
                                            ? AuditEventType.LIKE_CREATED
                                            : AuditEventType.LIKE_DELETED,
                                    likeEvent.userId(),
                                    likeEvent.productId()
                    )
                );
    });

        ack.acknowledge();
    }

    @KafkaListener(
            topics = "${demo-kafka.order.create.topic-name}",
            groupId = "logging-consumer-group",
            containerFactory = KafkaConfig.BATCH_LISTENER
    )
    public void onOrderCreateEvent(List<KafkaMessage<OrderCreatedKafkaEvent>> events, Acknowledgment ack) {
        events.stream()
                .filter(e -> eventHandlerService.tryConsume(e.eventId(), "logging"))
                .forEach(message -> {
                    OrderCreatedKafkaEvent event = message.payload();
                    auditLogService.save(
                            new AuditLogCommand.AuditLog(
                                    message.eventId(),
                                    AuditEventType.ORDER_CREATED,
                                    event.userId(),
                                    event.orderId()
                            )
                    );
                });

        ack.acknowledge();
    }

    @KafkaListener(
            topics = "${demo-kafka.order.failed.topic-name}",
            groupId = "logging-consumer-group",
            containerFactory = KafkaConfig.BATCH_LISTENER
    )
    public void onOrderFailedEvent(List<KafkaMessage<OrderCreatedEvent>> events, Acknowledgment ack) {
        events.stream()
                .filter(e -> eventHandlerService.tryConsume(e.eventId(), "logging"))
                .forEach(message -> {
                    OrderCreatedEvent event = message.payload();
                    auditLogService.save(
                            new AuditLogCommand.AuditLog(
                                    message.eventId(),
                                    AuditEventType.ORDER_FAILED,
                                    event.userId(),
                                    event.orderId()
                            )
                    );
                });

        ack.acknowledge();
    }

    @KafkaListener(
            topics = "${demo-kafka.payment.failed.topic-name}",
            groupId = "logging-consumer-group",
            containerFactory = KafkaConfig.BATCH_LISTENER
    )
    public void onPaymentFailedEvent(List<KafkaMessage<PaymentFailedEvent>> events, Acknowledgment ack) {
        events.stream()
                .filter(e -> eventHandlerService.tryConsume(e.eventId(), "logging"))
                .forEach(message -> {
                    PaymentFailedEvent event = message.payload();
                    auditLogService.save(
                            new AuditLogCommand.AuditLog(
                                    message.eventId(),
                                    AuditEventType.PAYMENT_FAILED,
                                    event.userId(),
                                    event.paymentId()
                            )
                    );
                });

        ack.acknowledge();
    }

    @KafkaListener(
            topics = "${demo-kafka.payment.confirmed.topic-name}",
            groupId = "logging-consumer-group",
            containerFactory = KafkaConfig.BATCH_LISTENER
    )
    public void onPaymentConfirmedEvent(List<KafkaMessage<PaymentConfirmedEvent>> events, Acknowledgment ack) {
        events.stream()
                .filter(e -> eventHandlerService.tryConsume(e.eventId(), "logging"))
                .forEach(message -> {
                    PaymentConfirmedEvent event = message.payload();
                    auditLogService.save(
                            new AuditLogCommand.AuditLog(
                                    message.eventId(),
                                    AuditEventType.PAYMENT_SUCCESS,
                                    event.userId(),
                                    event.paymentId()
                            )
                    );
                });

        ack.acknowledge();
    }


}
