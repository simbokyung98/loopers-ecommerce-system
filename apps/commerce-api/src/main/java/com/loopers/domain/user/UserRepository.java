package com.loopers.domain.user;

public interface UserRepository {
    UserModel save(UserModel userModel);

    Boolean existsByLoginId(String loginId);

    UserModel findByLoginId(String userId);
}
