package com.loopers.domain.point;

import com.loopers.domain.user.UserModel;
import com.loopers.infrastructure.point.PointJpaRepository;
import com.loopers.infrastructure.user.UserJpaRepository;
import com.loopers.interfaces.api.User.Gender;
import com.loopers.utils.DatabaseCleanUp;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

@SpringBootTest
class PointServiceIntegrationTest {

    @Autowired
    private PointService pointService;


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

    @DisplayName("내 포인트를 조회할 때")
    @Nested
    class Get{
        /**
         * - [ ]  해당 ID 의 회원이 존재할 경우, 보유 포인트가 반환된다.
         * - [ ]  해당 ID 의 회원이 존재하지 않을 경우, null 이 반환된다.
         */

        @DisplayName("해당 ID의 회원이 존재할 경우, 보유 포인트가 반환된다")
        @Test
        void returnsCurrentPoint_whenUserExistsById() {
            //arrange
            UserModel signInModel =  new UserModel("testId", Gender.MALE.getCode(), "2024-05-22", "loopers@test.com");
            userJpaRepository.save(signInModel);

            PointModel pointModel = pointJpaRepository.save(new PointModel(1L));

            //act
            PointModel result = pointService.get(1L);

            //assert
            assertAll(
                    () -> assertThat(result).isNotNull(),
                    () -> assertThat(result.getTotalAmount()).isEqualTo(pointModel.getTotalAmount())
            );
        }

        @DisplayName("해당 ID의 회원이 존재하지 않을 경우, null이 반환된다")
        @Test
        void returnsNull_whenUserDoesNotExistById() {

            //act
            PointModel result = pointService.get(1L);

            //assert
            assertThat(result).isNull();
        }
    }


}
