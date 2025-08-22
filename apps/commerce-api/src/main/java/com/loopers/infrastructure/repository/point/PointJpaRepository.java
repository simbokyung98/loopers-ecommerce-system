package com.loopers.infrastructure.repository.point;

import com.loopers.domain.point.PointModel;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface PointJpaRepository extends JpaRepository<PointModel, Long> {

    Optional<PointModel> findByUserId(Long userId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM PointModel p WHERE p.userId = :userId")
    Optional<PointModel> findByUserIdForUpdate(@Param("userId") Long userId);
}
