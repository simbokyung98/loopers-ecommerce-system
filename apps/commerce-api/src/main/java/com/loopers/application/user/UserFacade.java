package com.loopers.application.user;

import com.loopers.domain.point.PointService;
import com.loopers.domain.user.UserModel;
import com.loopers.domain.user.UserService;
import com.loopers.interfaces.api.User.Gender;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Component
public class UserFacade {
    private final UserService userService;
    private final PointService pointService;

    @Transactional
    public UserInfo signIn(String loginId, Gender gender, String brith, String email){

        UserModel userModel = userService.save(loginId, gender, brith, email);
        pointService.create(userModel.getId());
        return UserInfo.from(userModel);
    }

    public UserInfo getByLoginId(String loginId){
        UserModel userModel = userService.getUserByLoginId(loginId);
        if(userModel == null){
            throw new CoreException(ErrorType.BAD_REQUEST, "존재하지 않는 사용자 입니다.");
        }
        return UserInfo.from(userModel);
    }
}
