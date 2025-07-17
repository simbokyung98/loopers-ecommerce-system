package com.loopers.domain.point;


import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Component
public class PointService {

    private final PointRepository pointRepository;

    @Transactional
    public PointModel charge(Long userId, Long point){
        PointModel pointModel = pointRepository.findByUserId(userId);

        if(pointModel == null){
         pointModel = pointRepository.save(new PointModel(userId, 0L));
        }

        pointModel.charge(point);

        return pointRepository.save(pointModel);

    }

    @Transactional(readOnly = true)
    public Long getPointAmount(Long userId){
        PointModel pointModel = pointRepository.findByUserId(userId);
        return pointModel != null? pointModel.getPoint() : null;
    }
}
