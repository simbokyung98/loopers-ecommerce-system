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
        PointModel pointModel = pointRepository.findByUserId(userId)
                .orElseGet(() -> pointRepository.save(new PointModel(userId, 0L)));


        pointModel.charge(point);

        return pointRepository.save(pointModel);


    }
}
