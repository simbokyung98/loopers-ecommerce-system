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
                    new OrderModel(1L, 1L, "testAddress", phoneNumber, "testName", OrderStatus.PAID)
            );

            assertThat(throwable)
                    .isInstanceOf(CoreException.class)
                    .extracting("errorType")
                    .isEqualTo(ErrorType.BAD_REQUEST);
        }

        @DisplayName("주문 가격이 음수이면 , BAD_REQUEST 예외가 발생한다.")
        @Test
        void throwsBadRequestException_whenAmountIsNegative(){

            Long negativeAmount = -1L;


            Throwable throwable = catchThrowable(() ->
                    new OrderModel(1L, negativeAmount, "testAddress", "01011112222", "testName", OrderStatus.PAID)
            );

            assertThat(throwable)
                    .isInstanceOf(CoreException.class)
                    .extracting("errorType")
                    .isEqualTo(ErrorType.BAD_REQUEST);
        }
    }
}
