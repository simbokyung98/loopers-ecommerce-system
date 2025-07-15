package com.loopers.application.user;

import com.loopers.domain.user.UserModel;
import com.loopers.interfaces.api.User.Gender;
import com.loopers.interfaces.api.User.UserV1Dto;


public class UserVo {
    public record UserInfo(
            Long id,
            String loginId,
            Gender gender,
            String brith,
            String email) {
        public static UserInfo from(UserModel model) {
            return new UserInfo(
                    model.getId(),
                    model.getLoginId(),
                    Gender.fromCode(model.getGender()),
                    model.getBrith(),
                    model.getEmail()
            );
        }

    }

    public record UserCommand(
            String loginId,
            Gender gender,
            String brith,
            String email
    ){
        public static UserCommand from(UserV1Dto.SignInRequest signInRequest){
            return new UserCommand(
                    signInRequest.loginId(),
                    Gender.fromCode(signInRequest.gender()),
                    signInRequest.brith(),
                    signInRequest.email()
            );
        }

        public UserModel toModel(){
            return new UserModel(loginId, gender.getCode(), brith, email);
        }
    }

}
