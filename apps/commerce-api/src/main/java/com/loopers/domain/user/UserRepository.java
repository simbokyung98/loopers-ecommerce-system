package com.loopers.domain.user;

public interface UserRepository {
    UserModel save(UserModel userModel);

    Boolean existsByLoginId(String loginId);

    Boolean existsById(Long id);

    UserModel findByLoginId(String userId);

}
