package com.loopers.domain.log;


public class AuditLogCommand {

    public record AuditLog(
            String eventId,
            AuditEventType eventType,
            Long userId,
            Long targetId
    ){}
}
