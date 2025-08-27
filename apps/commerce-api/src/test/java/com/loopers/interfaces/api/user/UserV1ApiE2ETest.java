package com.loopers.interfaces.api.user;

import com.loopers.domain.user.UserModel;
import com.loopers.infrastructure.repository.user.UserJpaRepository;
import com.loopers.interfaces.api.ApiResponse;
import com.loopers.interfaces.api.User.Gender;
import com.loopers.interfaces.api.User.UserV1Dto;
import com.loopers.utils.DatabaseCleanUp;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.test.context.TestConstructor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
@RequiredArgsConstructor
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class UserV1ApiE2ETest {

    @Autowired
    private TestRestTemplate testRestTemplate;
    @Autowired
    private UserJpaRepository userJpaRepository;
    @Autowired
    private DatabaseCleanUp databaseCleanUp;

    @AfterEach
    void tearDown() {
        databaseCleanUp.truncateAllTables();
    }

    @DisplayName("POST /api/v1/users")
    @Nested
    class SignIn {
        /**
         * - [v]  회원 가입이 성공할 경우, 생성된 유저 정보를 응답으로 반환한다.
         * - [v]  회원 가입 시에 성별이 없을 경우, `400 Bad Request` 응답을 반환한다.
         */


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

    @DisplayName("Get /api/v1/user/me")
    @Nested
    class Get {

        /**
         * - [ ]  내 정보 조회에 성공할 경우, 해당하는 유저 정보를 응답으로 반환한다.
         * - [ ]  존재하지 않는 ID 로 조회할 경우, `404 Not Found` 응답을 반환한다.
         */
        private static final String ENDPOINT_GET = "/api/v1/users/me";

        @DisplayName("내 정보 조회에 성공할 경우, 해당하는 유저 정보를 응답으로 반환한다.")
        @Test
        void  returnsUserInfo_whenUserIdExists(){
            //arrange
            UserModel signInModel =  new UserModel("testId", Gender.MALE.getCode(), "2024-05-22", "loopers@test.com");
            userJpaRepository.save(signInModel);

            HttpHeaders headers = new HttpHeaders();
            headers.set("X-USER-ID", "testId");
            headers.setContentType(MediaType.APPLICATION_JSON);

            //act
            ParameterizedTypeReference<ApiResponse<UserV1Dto.UserResponse>> responseType = new ParameterizedTypeReference<>() {};
            ResponseEntity<ApiResponse<UserV1Dto.UserResponse>> response =
                    testRestTemplate.exchange(ENDPOINT_GET, HttpMethod.GET, new HttpEntity<>(headers), responseType);



            //assert
            assertAll(
                    () -> assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK),
                    () -> assertThat(response.getBody()).isNotNull(),
                    () -> assertThat(response.getBody().data().loginId()).isEqualTo(signInModel.getLoginId()),
                    () -> assertThat(response.getBody().data().gender()).isEqualTo(signInModel.getGender()),
                    () -> assertThat(response.getBody().data().brith()).isEqualTo(signInModel.getBrith()),
                    () -> assertThat(response.getBody().data().email()).isEqualTo(signInModel.getEmail())
            );
        }

        @DisplayName("존재하지 않는 ID 로 조회할 경우, `404 Not Found` 응답을 반환한다.")
        @Test
        void  throwsBadRequest_whenUserIdDoesNotExist(){

            //arrange
            HttpHeaders headers = new HttpHeaders();
            headers.set("X-USER-ID", "testId");
            headers.setContentType(MediaType.APPLICATION_JSON);

            //act
            ParameterizedTypeReference<ApiResponse<UserV1Dto.UserResponse>> responseType = new ParameterizedTypeReference<>() {};
            ResponseEntity<ApiResponse<UserV1Dto.UserResponse>> response =
                    testRestTemplate.exchange(ENDPOINT_GET, HttpMethod.GET, new HttpEntity<>(headers), responseType);


            //assert
            assertAll(
                    () -> assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST),
                    () -> assertThat(response.getBody().meta().result()).isEqualTo(ApiResponse.Metadata.Result.FAIL)
            );

        }
    }

}
