package com.loopers.infrastructure.point;

import com.loopers.domain.point.PointModel;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PointJpaRepository extends JpaRepository<PointModel, Long> {

    PointModel findByUserId(Long userId);
}
