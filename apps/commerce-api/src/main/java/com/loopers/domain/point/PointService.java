package com.loopers.domain.point;


import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class PointService {

    private final PointRepository pointRepository;

    public PointModel create(Long userId){
        PointModel pointModel = new PointModel(userId);
        return pointRepository.save(pointModel);
    }

    public PointModel charge(Long userId, Long point){
        PointModel pointModel = pointRepository.findByUserId(userId);

//        if(pointModel == null){
//         pointModel = pointRepository.save(new PointModel(userId, 0L));
//        }

        pointModel.charge(point);

        return pointRepository.save(pointModel);

    }

    @Transactional(readOnly = true)
    public PointModel get(Long userId){
        return pointRepository.findByUserId(userId);
    }
}
