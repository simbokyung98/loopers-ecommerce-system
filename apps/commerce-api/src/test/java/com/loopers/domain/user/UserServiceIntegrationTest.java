package com.loopers.domain.user;

import com.loopers.infrastructure.example.ExampleJpaRepository;
import com.loopers.infrastructure.user.UserJpaRepository;
import com.loopers.interfaces.api.User.Gender;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import com.loopers.utils.DatabaseCleanUp;
import org.instancio.Model;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

import static org.assertj.core.api.Assertions.as;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

@SpringBootTest
public class UserServiceIntegrationTest {

    /**
     * - [v]  회원 가입시 User 저장이 수행된다. ( spy 검증 )
     * - [v]  이미 가입된 ID 로 회원가입 시도 시, 실패한다.
     * */

    @Autowired
    private UserService userService;

    @MockitoSpyBean
    private UserRepository userRepository;

    @Autowired
    private DatabaseCleanUp databaseCleanUp;

    @AfterEach
    void tearDown() {
        databaseCleanUp.truncateAllTables();
    }

    @DisplayName("회원가입을 할 때, ")
    @Nested
    class SignIn {

        @DisplayName("회원 가입시 User 저장이 수행된다. ( spy 검증 )")
        @Test
        void returnUserInfo_whenSignIn() {
            // arrange
            UserModel userModel =  new UserModel(
                    "12345678",
                    Gender.MALE.getCode(),
                    "2025-05-22",
                    "loopers@test.com");


            // act
            UserModel result = userService.save(userModel);

            // assert
            verify(userRepository).save(userModel);
        }

        @DisplayName("이미 가입된 ID 로 회원가입 시도 시, 실패한다.")
        @Test
        void throwsException_whenAlreadySignedIn() {
            // arrange
            UserModel userModel = userRepository.save(
                    new UserModel("심보경", Gender.MALE.getCode(), "2024-05-22", "loopers@test.com")
            );

            // act
            CoreException exception = assertThrows(CoreException.class, () -> {
                userService.save(userModel);
            });

            // assert
            assertThat(exception.getErrorType()).isEqualTo(ErrorType.BAD_REQUEST);
        }

    }
}
