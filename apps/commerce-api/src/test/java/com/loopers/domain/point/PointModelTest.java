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

        @DisplayName("포인트 충전을 요청한 경우, 요청 포인트만큼 포인트를 충전한다")
        @Test
        void chargePoint_whenChargeRequest() {
            Long chargePoint = 100L;

            PointModel pointModel = new PointModel(1L);
            pointModel.charge(chargePoint);

            assertThat(pointModel.getTotalAmount()).isEqualTo(chargePoint);
        }

    }


    @DisplayName("포인트 사용 시,")
    @Nested
    class Spend {

        @DisplayName("0 이하의 정수로 포인트 사용시, BadRequest 예외가 발생한다")
        @Test
        void throwsBadRequestException_whenSpendAmountIsZeroOrNegative() {
            PointModel pointModel = new PointModel(1L);

            Throwable thrown = catchThrowable(() -> pointModel.spand(-2L));

            assertThat(thrown)
                    .isInstanceOf(CoreException.class)
                    .extracting("errorType")
                    .isEqualTo(ErrorType.BAD_REQUEST);
        }


        @DisplayName("포인트가 부족할경우, IllegalStateException 예외가 발생한다")
        @Test
        void throwsIllegalStateException_whenTotalAmountNotEnough() {
            PointModel pointModel = new PointModel(1L);
            pointModel.charge(100L);

            Throwable thrown = catchThrowable(() -> pointModel.spand(101L));

            assertThat(thrown)
                    .isInstanceOf(IllegalStateException.class);
        }

        @DisplayName("포인트가 충분 할 경우, 포인트를 사용한다")
        @Test
        void spendPoint_whenTotalAmountEnough() {
            Long chargePoint = 100L;
            Long sendPoint = 99L;
            PointModel pointModel = new PointModel(1L);
            pointModel.charge(chargePoint);

            pointModel.spand(sendPoint);

            assertThat(pointModel.getTotalAmount()).isEqualTo(chargePoint -sendPoint);
        }

    }


}
