package com.loopers.infrastructure.log;

import com.loopers.domain.log.AuditLogModel;
import com.loopers.domain.log.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@RequiredArgsConstructor
@Repository
public class AuditLogRepositoryImpl implements AuditLogRepository {

    private final AuditLogJpaRepository auditLogJpaRepository;

    @Override
    public void save(AuditLogModel auditLogModel) {
        auditLogJpaRepository.save(auditLogModel);
    }
}
