package com.loopers.application.point;

import com.loopers.domain.point.PointModel;
import com.loopers.domain.point.PointService;
import com.loopers.domain.user.UserService;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class PointFacade {

    private final UserService userService;
    private final PointService pointService;


    public Long charge(Long userId, Long point){

        userService.checkExist(userId);

        PointModel pointModel = pointService.charge(userId, point);

        return pointModel.getAmount();
    }

    public Long getPointAmount(Long userId){

        userService.checkExist(userId);
        PointModel pointModel = pointService.get(userId);

        if(pointModel == null){
            throw new CoreException(ErrorType.BAD_REQUEST, "사용자 포인트 정보가 존재하지 않습니다..");
        }
        return pointModel.getAmount();
    }

}
