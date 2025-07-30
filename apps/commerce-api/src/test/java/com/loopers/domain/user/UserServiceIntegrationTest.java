package com.loopers.domain.user;

import com.loopers.interfaces.api.User.Gender;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import com.loopers.utils.DatabaseCleanUp;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatException;
import static org.assertj.core.api.InstanceOfAssertFactories.type;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;

@SpringBootTest
public class UserServiceIntegrationTest {


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

        /**
         * - [v]  회원 가입시 User 저장이 수행된다. ( spy 검증 )
         * - [v]  이미 가입된 ID 로 회원가입 시도 시, 실패한다.
         * */

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
           userService.save(
                   "12345678",
                   Gender.MALE,
                   "2025-05-22",
                   "loopers@test.com"
           );

            // assert
            verify(userRepository).save(any(UserModel.class));
        }

        @DisplayName("이미 가입된 ID 로 회원가입 시도 시, BAD_REQUEST 를 반환한다.")
        @Test
        void throwsException_whenAlreadySignedIn() {
            // arrange
            UserModel userModel = userRepository.save(
                    new UserModel("심보경", Gender.MALE.getCode(), "2024-05-22", "loopers@test.com")
            );

            // act
            CoreException exception = assertThrows(CoreException.class,
                    () -> userService.save("심보경", Gender.MALE, "2024-05-22", "loopers@test.com"));

            // assert
            assertThat(exception.getErrorType()).isEqualTo(ErrorType.BAD_REQUEST);
        }

    }

    @DisplayName("내 정보를 조회 할 때, ")
    @Nested
    class Get{
        /**
         * - [ ]  해당 ID 의 회원이 존재할 경우, 회원 정보가 반환된다.
         * - [ ]  해당 ID 의 회원이 존재하지 않을 경우, null 이 반환된다.
         */

        @DisplayName("해당 ID 의 회원이 존재할 경우, 회원 정보가 반환된다.")
        @Test
        void returnsUserInfo_whenUserExists(){
            // arrange
            UserModel requestModel = new UserModel(
                    "testId",
                    Gender.MALE.getCode(),
                    "2024-05-22",
                    "loopers@test.com"
            );
            userRepository.save(requestModel);

            // act
            UserModel result = userService.getByLoginId(requestModel.getLoginId());

            // assert
            assertAll(
                    () -> assertThat(result).isNotNull(),
                    () -> assertThat(result.getLoginId()).isEqualTo(requestModel.getLoginId()),
                    () -> assertThat(result.getGender()).isEqualTo(requestModel.getGender()),
                    () -> assertThat(result.getBrith()).isEqualTo(requestModel.getBrith()),
                    () -> assertThat(result.getEmail()).isEqualTo(requestModel.getEmail())
            );
        }

        @DisplayName("해당 ID 의 회원이 존재하지 않을 경우, null 이 반환된다.")
        @Test
        void returnsNull_whenUserDoesNotExist(){
            // arrange
            String loginId = "testId";

            // act
            UserModel result = userService.getByLoginId(loginId);

            // assert
            assertThat(result).isNull();
        }
    }

    @DisplayName("유저 정보가 존재하는지 확인 할 때, ")
    @Nested
    class Exist {
        @DisplayName("존재하지 않는 유저 ID 로 좋아요를을 시도한 경우, BAD_REQUEST 예외가 발생하며 실패한다.")
        @Test
        void throwsException_whenUserDoesNotExist(){
            //arrange
            Long notExistUserId = 999L;



            assertThatException()
                    .isThrownBy(() -> userService.checkExist(notExistUserId))
                    .isInstanceOf(CoreException.class)
                    .extracting("errorType", type(ErrorType.class))
                    .isEqualTo(ErrorType.BAD_REQUEST);
        }
    }
}
