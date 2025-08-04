package com.loopers.infrastructure.point;

import com.loopers.domain.point.PointModel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PointJpaRepository extends JpaRepository<PointModel, Long> {

    Optional<PointModel> findByUserId(Long userId);
}
