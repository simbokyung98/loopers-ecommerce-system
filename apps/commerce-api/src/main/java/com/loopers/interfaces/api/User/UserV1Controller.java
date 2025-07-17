package com.loopers.interfaces.api.User;

import com.loopers.application.user.UserFacade;
import com.loopers.application.user.UserInfo;
import com.loopers.interfaces.api.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/users")
public class UserV1Controller implements UserV1ApiSpec {

    private final UserFacade userFacade;
    @PostMapping
    @Override
    public ApiResponse<UserV1Dto.UserResponse> SignIn(@RequestBody UserV1Dto.SignInRequest signInRequest) {

        UserInfo userInfo = userFacade.signIn(
                signInRequest.loginId(),
                Gender.fromCode(signInRequest.gender()),
                signInRequest.brith(),
                signInRequest.email()
        );

        UserV1Dto.UserResponse userResponse = UserV1Dto.UserResponse.from(userInfo);

        return ApiResponse.success(userResponse);
    }

    @Override
    @GetMapping("/me")
    public ApiResponse<UserV1Dto.UserResponse> getByLoginId(String loginId) {

        UserInfo userInfo = userFacade.getByLoginId(loginId);
        UserV1Dto.UserResponse userResponse = UserV1Dto.UserResponse.from(userInfo);

        return ApiResponse.success(userResponse);
    }
}
