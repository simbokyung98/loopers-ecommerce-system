package com.loopers.domain.order;

import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

class OrderItemModelTest {

    @DisplayName("주문 아이템 생성 시,")
    @Nested
    class create{

        @DisplayName("상품가격이 음수이면, BAD_REQUEST 예외가 발생한다.")
        @Test
        void throwsBadRequestException_whenPriceIsNegative(){
            Long negativePrice = -1L;

            Throwable throwable = catchThrowable(() ->
                    new OrderItemModel(1L, 1L, "testName", negativePrice, 1L)
            );

            assertThat(throwable)
                    .isInstanceOf(CoreException.class)
                    .extracting("errorType")
                    .isEqualTo(ErrorType.BAD_REQUEST);
        }

        @DisplayName("주문 가격이 음수이면 , BAD_REQUEST 예외가 발생한다.")
        @Test
        void throwsBadRequestException_whenQuantityIsNegative(){

            Long negativeQuantity = -1L;

            Throwable throwable = catchThrowable(() ->
                    new OrderItemModel(1L, 1L, "testName", 1L, negativeQuantity)
            );

            assertThat(throwable)
                    .isInstanceOf(CoreException.class)
                    .extracting("errorType")
                    .isEqualTo(ErrorType.BAD_REQUEST);
        }
    }
}
