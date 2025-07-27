package com.loopers.infrastructure.user;

import com.loopers.domain.user.UserModel;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserJpaRepository extends JpaRepository<UserModel, Long> {
    Boolean existsByLoginId(String loginId);

    UserModel findByLoginId(String loginId);
}
