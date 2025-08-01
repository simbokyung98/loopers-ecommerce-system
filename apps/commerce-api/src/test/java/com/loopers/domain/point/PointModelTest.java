package com.loopers.domain.point;


import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;


class PointModelTest {

    @DisplayName("포인트 충전 시,")
    @Nested
    class Charge {
        /**
         * 0 이하의 정수로 포인트를 충전 시 실패한다.
         */

        @DisplayName("0 이하의 정수로 포인트 충전 시, BadRequest 예외가 발생한다")
        @Test
        void throwsBadRequestException_whenChargeAmountIsZeroOrNegative() {
            PointModel pointModel = new PointModel(1L);

            Throwable thrown = catchThrowable(() -> pointModel.charge(-2L));

            assertThat(thrown)
                    .isInstanceOf(CoreException.class)
                    .extracting("errorType")
                    .isEqualTo(ErrorType.BAD_REQUEST);
        }

    }


}
