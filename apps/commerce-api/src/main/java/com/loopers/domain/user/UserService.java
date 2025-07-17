package com.loopers.domain.user;

import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

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

    @Transactional(readOnly = true)
    public UserModel getByLoginId(String loginId){
        return userRepository.findByLoginId(loginId);
    }


    @Transactional(readOnly = true)
    public Boolean existsById(Long id){
        return userRepository.existsById(id);
    }


}
