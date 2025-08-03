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

        userService.checkExistUser(userId);

        PointModel pointModel = pointService.charge(userId, point);

        return pointModel.getTotalAmount();
    }

    public Long getPointAmount(Long userId){

        userService.checkExistUser(userId);
        PointModel pointModel = pointService.getPoint(userId);

        if(pointModel == null){
            throw new CoreException(ErrorType.BAD_REQUEST, "사용자 포인트 정보가 존재하지 않습니다..");
        }
        return pointModel.getTotalAmount();
    }

}
