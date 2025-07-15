package com.loopers.application.user;

import com.loopers.domain.user.UserModel;
import com.loopers.domain.user.UserService;
import com.loopers.interfaces.api.User.Gender;
import com.loopers.interfaces.api.User.UserV1Dto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class UserFacade {
    private final UserService userService;

    public UserVo.UserInfo signIn(UserVo.UserCommand userCommand){

        UserModel userModel = userService.save(userCommand.toModel());
        return UserVo.UserInfo.from(userModel);
    }
}
