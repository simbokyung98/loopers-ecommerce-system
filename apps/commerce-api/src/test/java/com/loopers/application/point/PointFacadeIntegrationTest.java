package com.loopers.application.point;

import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThatException;
import static org.assertj.core.api.InstanceOfAssertFactories.type;

@SpringBootTest
class PointFacadeIntegrationTest {

    @Autowired
    private PointFacade pointFacade;


    @DisplayName("포인트를 충전을 할 때, ")
    @Nested
    class Charge {
        /**
         * - [v]  존재하지 않는 유저 ID 로 충전을 시도한 경우, 실패한다.
         */

        @DisplayName("존재하지 않는 유저 ID 로 충전을 시도한 경우, BAD_REQUEST 예외가 발생하며 실패한다.")
        @Test
        void throwsException_whenUserDoesNotExist(){

            Long userId = 1L;
            Long point = 1000L;

            assertThatException()
                    .isThrownBy(() -> pointFacade.charge(userId, point))
                    .isInstanceOf(CoreException.class)
                    .extracting("errorType", type(ErrorType.class))
                    .isEqualTo(ErrorType.BAD_REQUEST);
        }
    }
}
