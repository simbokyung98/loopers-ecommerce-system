package com.loopers.domain.log;

public enum AuditEventType {
    LIKE_CREATED,
    LIKE_DELETED,
    ORDER_CREATED,
    PAYMENT_SUCCESS,
    PAYMENT_FAILED;

    public String eventType() {
        return this.name();
    }
}
