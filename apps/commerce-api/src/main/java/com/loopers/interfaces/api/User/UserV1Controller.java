package com.loopers.interfaces.api.User;

import com.loopers.application.user.UserFacade;
import com.loopers.application.user.UserVo;
import com.loopers.interfaces.api.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/users")
public class UserV1Controller implements UserV1ApiSpec {

    private final UserFacade userFacade;
    @PostMapping
    @Override
    public ApiResponse<UserV1Dto.UserResponse> SignIn(@RequestBody UserV1Dto.SignInRequest signInRequest) {

        UserVo.UserInfo userInfo = userFacade.signIn(UserVo.UserCommand.from(signInRequest));
        UserV1Dto.UserResponse userResponse = UserV1Dto.UserResponse.from(userInfo);

        return ApiResponse.success(userResponse);
    }
}
