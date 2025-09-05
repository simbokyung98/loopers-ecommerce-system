package com.loopers.infrastructure.log;


import com.loopers.domain.log.AuditLogModel;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuditLogJpaRepository  extends JpaRepository<AuditLogModel, Long> {
}
