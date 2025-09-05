package com.loopers.domain.log;


public interface AuditLogRepository {

    void save(AuditLogModel auditLogModel);
}
