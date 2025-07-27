package com.loopers.infrastructure.user;

import com.loopers.domain.user.UserModel;
import com.loopers.domain.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@RequiredArgsConstructor
@Repository
public class UserRepositoryImpl implements UserRepository{

    private final UserJpaRepository userJpaRepository;

    @Override
    public UserModel save(UserModel userModel) {
        return userJpaRepository.save(userModel);
    }

    @Override
    public Boolean existsByLoginId(String loginId) {
        return userJpaRepository.existsByLoginId(loginId);
    }

    @Override
    public Boolean existsById(Long id) {
        return userJpaRepository.existsById(id);
    }

    @Override
    public UserModel findByLoginId(String loginId) {
        return userJpaRepository.findByLoginId(loginId);
    }
}
