package com.loopers.domain.point;


import java.util.Optional;

public interface PointRepository {

    PointModel save(PointModel pointModel);

    Optional<PointModel> findByUserId(Long userId);

    Optional<PointModel> findByUserIdWithPessimisticLock(Long userId);
}
