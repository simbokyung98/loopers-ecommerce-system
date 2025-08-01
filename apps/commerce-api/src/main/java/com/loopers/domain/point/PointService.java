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

    public PointModel charge(Long userId, Long amount){
        PointModel pointModel = pointRepository.findByUserId(userId);

        pointModel.charge(amount);

        return pointRepository.save(pointModel);

    }

    @Transactional(readOnly = true)
    public PointModel get(Long userId){
        return pointRepository.findByUserId(userId);
    }

    public void spend(Long userId, Long amount){
        PointModel pointModel = pointRepository.findByUserId(userId);
        pointModel.spand(amount);

        pointRepository.save(pointModel);


    }
}
