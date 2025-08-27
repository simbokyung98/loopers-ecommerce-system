package com.loopers.domain.order;

import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

class OrderModelTest {

    @DisplayName("주문 생성 시,")
    @Nested
    class create{

        @DisplayName("핸드폰 번호가 형식에 맞지 않으면, BAD_REQUEST 예외가 발생한다.")
        @ParameterizedTest
        @ValueSource(strings = {"123456", "010-1234-5678"})
        void throwsBadRequestException_whenPhoneNumberIsNotMatchingPattern(String phoneNumber){


            Throwable throwable = catchThrowable(() ->
                    new OrderModel(
                            1L,
                            null,
                            10000L,
                            9000L,
                            "testAddress",
                            phoneNumber,
                            "testName"
                    )
            );

            assertThat(throwable)
                    .isInstanceOf(CoreException.class)
                    .extracting("errorType")
                    .isEqualTo(ErrorType.BAD_REQUEST);
        }

        @DisplayName("총 금액이 음수이면, BAD_REQUEST 예외가 발생한다.")
        @Test
        void throwsBadRequestException_whenTotalAmountIsNegative() {
            Long negativeAmount = -1L;

            Throwable throwable = catchThrowable(() ->
                    new OrderModel(
                            1L,
                            null,
                            negativeAmount,  // finalCount
                            0L,              // finalAmount
                            "testAddress",
                            "01011112222",
                            "testName"
                    )
            );

            assertThat(throwable)
                    .isInstanceOf(CoreException.class)
                    .extracting("errorType")
                    .isEqualTo(ErrorType.BAD_REQUEST);
        }

        @DisplayName("최종 결제 금액이 음수이면, BAD_REQUEST 예외가 발생한다.")
        @Test
        void throwsBadRequestException_whenFinalAmountIsNegative() {
            Throwable throwable = catchThrowable(() ->
                    new OrderModel(
                            1L,
                            null,
                            10000L,       // finalCount
                            -1L,          // finalAmount
                            "testAddress",
                            "01011112222",
                            "testName"
                    )
            );

            assertThat(throwable)
                    .isInstanceOf(CoreException.class)
                    .extracting("errorType")
                    .isEqualTo(ErrorType.BAD_REQUEST);
        }

        @DisplayName("최종 결제 금액이 총 금액보다 크면, BAD_REQUEST 예외가 발생한다.")
        @Test
        void throwsBadRequestException_whenFinalAmountGreaterThanTotalAmount() {
            Throwable throwable = catchThrowable(() ->
                    new OrderModel(
                            1L,
                            null,
                            10000L,       // finalCount
                            20000L,       // finalAmount
                            "testAddress",
                            "01011112222",
                            "testName"
                    )
            );

            assertThat(throwable)
                    .isInstanceOf(CoreException.class)
                    .extracting("errorType")
                    .isEqualTo(ErrorType.BAD_REQUEST);
        }
    }
}
