package com.loopers.infrastructure.point;

import com.loopers.domain.point.PointModel;
import com.loopers.domain.point.PointRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@RequiredArgsConstructor
@Repository
public class PointRepositoryImpl implements PointRepository {
    private final PointJpaRepository pointJpaRepository;


    @Override
    public PointModel save(PointModel pointModel) {
        return pointJpaRepository.save(pointModel);
    }

    @Override
    public Optional<PointModel> findByUserId(Long userId) {
        return pointJpaRepository.findByUserId(userId);
    }
}
