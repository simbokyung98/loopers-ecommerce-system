package com.loopers.application.user;

import com.loopers.domain.user.UserModel;
import com.loopers.interfaces.api.User.Gender;

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

