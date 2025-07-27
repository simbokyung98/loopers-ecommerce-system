package com.loopers.domain.user;

import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;


class UserModelTest {

    /***
     * - [v]  ID 가 `영문 및 숫자 10자 이내` 형식에 맞지 않으면, User 객체 생성에 실패한다.
     * - [v]  이메일이 `xx@yy.zz` 형식에 맞지 않으면, User 객체 생성에 실패한다.
     * - [v]  생년월일이 `yyyy-MM-dd` 형식에 맞지 않으면, User 객체 생성에 실패한다.
     */


    @DisplayName("유저 모델 생성 시, ")
    @Nested
    class Create {

        @DisplayName("ID 가 영문 및 숫자 10자 이내 형식에 맞지 않으면, User 객체 생성에 실패하여 BAD_REQUEST 예외가 발생한다.")
        @Test
        void throwsBadRequestException_whenLoginIdIsTooLong() {

            Throwable throwable = catchThrowable(() ->
                new UserModel("1234567890", "F", "2025-05-22", "loopers@test.com"));

            assertThat(throwable)
                    .isInstanceOf(CoreException.class)
                            .extracting("errorType")
                                    .isEqualTo(ErrorType.BAD_REQUEST);

        }

        @DisplayName("메일이 xx@yy.zz 형식에 맞지 않으면, User 객체 생성에 실패하여 BAD_REQUEST 예외가 발생한다.")
        @Test
        void throwsBadRequestException_whenEmailIsNotMatchingPattern() {

            Throwable throwable = catchThrowable(() ->
                new UserModel("123456789", "W", "2025-05-22", "loopers"));

            assertThat(throwable)
                    .isInstanceOf(CoreException.class)
                    .extracting("errorType")
                    .isEqualTo(ErrorType.BAD_REQUEST);


        }

        @DisplayName("생년월일이 `yyyy-MM-dd` 형식에 맞지 않으면, User 객체 생성에 실패하여 BAD_REQUEST 예외가 발생한다.")
        @Test
        void throwsBadRequestException_whenBirthDateIsNotMatchingPattern() {

            Throwable throwable = catchThrowable(() ->
                    new UserModel("123456789", "W", "20250522", "loopers@test.com"));

            assertThat(throwable)
                    .isInstanceOf(CoreException.class)
                    .extracting("errorType")
                    .isEqualTo(ErrorType.BAD_REQUEST);

        }



    }
}
