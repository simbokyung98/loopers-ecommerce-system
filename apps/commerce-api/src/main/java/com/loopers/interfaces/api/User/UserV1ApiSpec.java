package com.loopers.interfaces.api.User;

import com.loopers.interfaces.api.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "User V1 API", description = "User API 입니다.")
public interface UserV1ApiSpec {
    @Operation(summary = "회원 가입")
    ApiResponse<UserV1Dto.UserResponse> SignIn(
            UserV1Dto.SignInRequest signInRequest
    );
}
