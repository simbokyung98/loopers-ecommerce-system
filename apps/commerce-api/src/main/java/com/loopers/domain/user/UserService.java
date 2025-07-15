package com.loopers.domain.user;

import com.loopers.infrastructure.user.UserJpaRepository;
import com.loopers.interfaces.api.User.Gender;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class UserService {

    private final UserRepository userRepository;

    @Transactional
    public UserModel save(UserModel userModel) {

        if (userRepository.existsByLoginId(userModel.getLoginId())) {
            throw new CoreException(ErrorType.BAD_REQUEST, "이미 존재하는 사용자 입니다.");
        }
        return userRepository.save(userModel);
    }
}
