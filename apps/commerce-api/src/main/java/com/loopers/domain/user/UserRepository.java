package com.loopers.domain.user;

import com.loopers.interfaces.api.User.UserV1Dto;

import java.util.Optional;

public interface UserRepository {
    UserModel save(UserModel userModel);

    Boolean existsByLoginId(String loginId);
}
