package com.loopers.domain.point;


public interface PointRepository {

    PointModel save(PointModel pointModel);

    PointModel findByUserId(Long userId);
}
