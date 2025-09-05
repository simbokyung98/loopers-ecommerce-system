package com.loopers.domain.log;


import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuditLogService {

    private final AuditLogRepository repository;

    @Transactional
    public void save(AuditLogCommand.AuditLog command) {
        AuditLogModel log = new AuditLogModel(
                command.eventId(),
                command.eventType().eventType(),
                command.userId(),
                command.targetId());
        repository.save(log);
    }
}
