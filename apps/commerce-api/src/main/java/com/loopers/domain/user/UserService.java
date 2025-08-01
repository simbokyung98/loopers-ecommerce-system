package com.loopers.domain.user;

import com.loopers.interfaces.api.User.Gender;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class UserService {

    private final UserRepository userRepository;

    public UserModel save(String loginId, Gender gender, String brith, String email) {

        if (userRepository.existsByLoginId(loginId)) {
            throw new CoreException(ErrorType.BAD_REQUEST, "이미 존재하는 사용자 입니다.");
        }

        return userRepository.save(new UserModel(loginId, gender.getCode(), brith, email));
    }

    @Transactional(readOnly = true)
    public UserModel getByLoginId(String loginId){
        return userRepository.findByLoginId(loginId);
    }


    @Transactional(readOnly = true)
    public void checkExistUser(Long id){
        if(!userRepository.existsById(id)){
            throw new CoreException(ErrorType.BAD_REQUEST, "존재하지 않는 사용자 입니다.");
        }
    }


}
