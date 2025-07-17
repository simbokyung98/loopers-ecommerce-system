package com.loopers.application.point;

import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
public class PointFacadeIntegrationTest {

    @Autowired
    private PointFacade pointFacade;

    @DisplayName("포인트를 충전을 할 때, ")
    @Nested
    class Charge {
        /**
         * - [v]  존재하지 않는 유저 ID 로 충전을 시도한 경우, 실패한다.
         */

        @DisplayName("존재하지 않는 유저 ID 로 충전을 시도한 경우, 실패한다.")
        @Test
        void throwsException_whenUserDoesNotExist(){

            Long userId = 1L;
            Long point = 1000L;

            //act
            CoreException exception = assertThrows(CoreException.class, () -> pointFacade.charge(userId, point));

            //assert
            assertThat(exception.getErrorType()).isEqualTo(ErrorType.BAD_REQUEST);

        }
    }
}
