package com.loopers.domain.log;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "audit_log",
        uniqueConstraints = @UniqueConstraint(columnNames = {"eventId", "eventType"}))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AuditLogModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String eventId;    // Kafka 이벤트 UUID
    private String eventType;  // LIKE_CREATED, ORDER_CREATED, PAYMENT_SUCCESS 등
    private Long userId;       // 이벤트 주체
    private Long targetId;     // productId / orderId / paymentId 등

    private LocalDateTime createdAt = LocalDateTime.now();

    public AuditLogModel(String eventId, String eventType, Long userId, Long targetId) {
        this.eventId = eventId;
        this.eventType = eventType;
        this.userId = userId;
        this.targetId = targetId;
    }
}
