package com.loopers.interfaces.api.point;

import com.loopers.domain.point.PointModel;
import com.loopers.domain.user.UserModel;
import com.loopers.infrastructure.repository.point.PointJpaRepository;
import com.loopers.infrastructure.repository.user.UserJpaRepository;
import com.loopers.interfaces.api.ApiResponse;
import com.loopers.interfaces.api.User.Gender;
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
class PointV1ApiE2ETest {

    @Autowired
    private TestRestTemplate testRestTemplate;
    @Autowired
    private UserJpaRepository userJpaRepository;
    @Autowired
    private PointJpaRepository pointJpaRepository;
    @Autowired
    private DatabaseCleanUp databaseCleanUp;

    @AfterEach
    void tearDown() {
        databaseCleanUp.truncateAllTables();
    }

    @DisplayName("POST /api/v1/ponits/charge")
    @Nested
    class Charge {
        /**
         * - [ ]  존재하는 유저가 1000원을 충전할 경우, 충전된 보유 총량을 응답으로 반환한다.
         * - [ ]  존재하지 않는 유저로 요청할 경우, `404 Not Found` 응답을 반환한다.
         */

        private static final String ENDPOINT_POST = "/api/v1/points/charge";

        @DisplayName("존재하는 유저가 1000원을 충전할 경우, 충전된 보유 총량을 응답으로 반환한다.")
        @Test
        void returnsTotalPoint_whenChargingExistingUserWith1000Won(){
            //arrange
            UserModel signInModel =  new UserModel("testId", Gender.MALE.getCode(), "2024-05-22", "loopers@test.com");
            UserModel resultUser = userJpaRepository.save(signInModel);

            PointModel pointModel = new PointModel(resultUser.getId());
            pointModel.charge(1000L);

            pointJpaRepository.save(pointModel);

            HttpHeaders headers = new HttpHeaders();
            headers.set("X-USER-ID", "1");
            headers.setContentType(MediaType.APPLICATION_JSON);

            PointV1Dto.PointRequest pointRequest = new PointV1Dto.PointRequest(1000L);

            //act
            ParameterizedTypeReference<ApiResponse<Long>> responseType = new ParameterizedTypeReference<>() {};
            ResponseEntity<ApiResponse<Long>> response =
                    testRestTemplate.exchange(ENDPOINT_POST, HttpMethod.POST,  new HttpEntity<>(pointRequest, headers), responseType);

            //assert
            assertAll(
                    () -> assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK),
                    () -> assertThat(response.getBody()).isNotNull(),
                    () -> assertThat(response.getBody().data()).isEqualTo(2000L)
            );


        }

        @DisplayName("존재하지 않는 유저로 요청할 경우, 404 Not Found 응답을 반환한다.")
        @Test
        void throwsNotFound_whenChargingNonExistentUser() {

            //arrange
            String INVALIDE_USER = "2";
            HttpHeaders headers = new HttpHeaders();
            headers.set("X-USER-ID",INVALIDE_USER);
            headers.setContentType(MediaType.APPLICATION_JSON);

            PointV1Dto.PointRequest pointRequest = new PointV1Dto.PointRequest(1000L);

            //act
            ParameterizedTypeReference<ApiResponse<Long>> responseType = new ParameterizedTypeReference<>() {};
            ResponseEntity<ApiResponse<Long>> response =
                    testRestTemplate.exchange(ENDPOINT_POST, HttpMethod.POST, new HttpEntity<>(pointRequest, headers), responseType);

            //assert
            assertAll(
                    () -> assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST),
                    () -> assertThat(response.getBody().meta().result()).isEqualTo(ApiResponse.Metadata.Result.FAIL)
            );

        }



    }


    @DisplayName("Get /api/v1/points")
    @Nested
    class Get{
        /**
         * - [ ]  포인트 조회에 성공할 경우, 보유 포인트를 응답으로 반환한다.
         * - [ ]  `X-USER-ID` 헤더가 없을 경우, `400 Bad Request` 응답을 반환한다.
         */
        private static final String ENDPOINT_GET = "/api/v1/points";

        @DisplayName("포인트 조회에 성공할 경우, 보유 포인트를 응답으로 반환한다")
        @Test
        void returnsCurrentPoint_whenUserIdHeaderIsPresent() {
            //arrange
            UserModel signInModel =  new UserModel("testId", Gender.MALE.getCode(), "2024-05-22", "loopers@test.com");
            userJpaRepository.save(signInModel);

            PointModel pointModel = new PointModel(1L);
            pointModel.charge(2000L);

            pointJpaRepository.save(pointModel);

            HttpHeaders headers = new HttpHeaders();
            headers.set("X-USER-ID", "1");
            headers.setContentType(MediaType.APPLICATION_JSON);

            //act
            ParameterizedTypeReference<ApiResponse<Long>> responseType = new ParameterizedTypeReference<>() {};
            ResponseEntity<ApiResponse<Long>> response =
                    testRestTemplate.exchange(ENDPOINT_GET, HttpMethod.GET, new HttpEntity<>(headers), responseType);

            //assert
            assertAll(
                    () -> assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK),
                    () -> assertThat(response.getBody()).isNotNull(),
                    () -> assertThat(response.getBody().data()).isEqualTo(2000L)
            );
        }


        @DisplayName("`X-USER-ID` 헤더가 없을 경우, `400 Bad Request` 응답을 반환한다")
        @Test
        void throwsBadRequestException_whenUserIdHeaderIsMissing() {
            //arrange
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);


            //act
            ParameterizedTypeReference<ApiResponse<Long>> responseType = new ParameterizedTypeReference<>() {};
            ResponseEntity<ApiResponse<Long>> response =
                    testRestTemplate.exchange(ENDPOINT_GET, HttpMethod.GET, new HttpEntity<>(headers), responseType);

            //assert
            assertAll(
                    () -> assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST),
                    () -> assertThat(response.getBody().meta().result()).isEqualTo(ApiResponse.Metadata.Result.FAIL)
            );

        }


    }
}
