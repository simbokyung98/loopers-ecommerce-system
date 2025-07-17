package com.loopers.interfaces.api.User;

import com.loopers.application.user.UserInfo;
import jakarta.validation.constraints.NotNull;

public class UserV1Dto {

    public record UserResponse(
            Long id,
            String loginId,
            String gender,
            String brith,
            String email
    ){
        public static UserResponse from(UserInfo userInfo){
            return new UserResponse(
                    userInfo.id(),
                    userInfo.loginId(),
                    userInfo.gender().getCode(),
                    userInfo.brith(),
                    userInfo.email()
            );
        }
    }


    public record SignInRequest(

            @NotNull
            String loginId,
            @NotNull
            String gender,
            @NotNull
            String brith,
            @NotNull
            String email
    ){

    }

}
