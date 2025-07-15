package com.loopers.interfaces.api.user;

import com.loopers.interfaces.api.ApiResponse;
import com.loopers.interfaces.api.User.UserV1Dto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class UserV1ApiE2ETest {

    /**
     * - [ ]  회원 가입이 성공할 경우, 생성된 유저 정보를 응답으로 반환한다.
     * - [ ]  회원 가입 시에 성별이 없을 경우, `400 Bad Request` 응답을 반환한다.
     */

    @Autowired
    private TestRestTemplate testRestTemplate;

    @DisplayName("POST /api/v1/users")
    @Nested
    class SignIn {
        private static final String ENDPOINT_POST = "/api/v1/users";
        @DisplayName("회원 가입이 성공할 경우, 생성된 유저 정보를 응답으로 반환한다.")
        @Test
        void returnUserInfo_whenSignInSuccessful() {

            //arrange
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            UserV1Dto.SignInRequest signInRequest =
                    new UserV1Dto.SignInRequest(
                            "심보경",
                            "M",
                            "1999-01-01",
                            "test@test.com");

            //act
            ParameterizedTypeReference<ApiResponse<UserV1Dto.UserResponse>> responseType = new ParameterizedTypeReference<>() {};

            ResponseEntity<ApiResponse< UserV1Dto.UserResponse>> response =
                    testRestTemplate.exchange(ENDPOINT_POST, HttpMethod.POST,  new HttpEntity<>(signInRequest, headers), responseType);

            //assert
            assertAll(
                    () -> assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK),
                    () -> assertThat(response.getBody()).isNotNull(),
                    () -> assertThat(response.getBody().data().loginId()).isEqualTo(signInRequest.loginId()),
                    () -> assertThat(response.getBody().data().gender()).isEqualTo(signInRequest.gender()),
                    () -> assertThat(response.getBody().data().brith()).isEqualTo(signInRequest.brith()),
                    () -> assertThat(response.getBody().data().email()).isEqualTo(signInRequest.email())
            );
        }

        @DisplayName("회원 가입 시에 성별이 없을 경우, `400 Bad Request` 응답을 반환한다.")
        @Test
        void throwsBadRequest_whenGenderIsNotProvided() {
            //arrange
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            UserV1Dto.SignInRequest signInRequest =
                    new UserV1Dto.SignInRequest(
                            "심보경",
                            null,
                            "1999-01-01",
                            "test@test.com");


            //act
            ParameterizedTypeReference<ApiResponse<UserV1Dto.UserResponse>> responseType = new ParameterizedTypeReference<>() {};

            ResponseEntity<ApiResponse< UserV1Dto.UserResponse>> response =
                    testRestTemplate.exchange(ENDPOINT_POST, HttpMethod.POST,  new HttpEntity<>(signInRequest, headers), responseType);

            //assert
            assertAll(
                    () -> assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST),
                    () -> assertThat(response.getBody().meta().result()).isEqualTo(ApiResponse.Metadata.Result.FAIL)
            );
        }
    }
}
