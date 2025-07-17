package com.loopers.domain.point;


import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertThrows;


class PointModelTest {

    @DisplayName("포인트 충전 시,")
    @Nested
    class Charge {
        /**
         * 0 이하의 정수로 포인트를 충전 시 실패한다.
         */

        @DisplayName("0 이하의 정수로 신규 포인트 충전 시, BadRequest 예외가 발생한다")
        @Test
        void throwsBadRequestException_whenNewChargeAmountIsZeroOrNegative() {


            CoreException result = assertThrows(CoreException.class, () -> {
                new PointModel(1L, -1L);
            });

            assertThat(result.getErrorType()).isEqualTo(ErrorType.BAD_REQUEST);
        }

        @DisplayName("0 이하의 정수로 포인트 충전 시, BadRequest 예외가 발생한다")
        @Test
        void throwsBadRequestException_whenChargeAmountIsZeroOrNegative() {
            PointModel pointModel = new PointModel(1L, 100L);

            CoreException result = assertThrows(CoreException.class, () -> {
                pointModel.charge(-2L);
            });

            assertThat(result.getErrorType()).isEqualTo(ErrorType.BAD_REQUEST);
        }

    }


}
