package com.loopers.interfaces.api.User;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.loopers.application.user.UserVo;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public class UserV1Dto {

    public record UserResponse(
            Long id,
            String loginId,
            String gender,
            String brith,
            String email
    ){
        public static UserResponse from(UserVo.UserInfo userInfo){
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
