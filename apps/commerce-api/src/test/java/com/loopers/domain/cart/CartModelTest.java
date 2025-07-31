package com.loopers.domain.cart;

import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

class CartModelTest {

    @DisplayName("장바구니 생성 시,")
    @Nested
    class create{


        @DisplayName("장바구니 수량이 음수 이면, BAD_REQUEST 예외가 발생한다.")
        @Test
        void throwsBadRequestException_whenQuantityIsNegative(){


            Long negativeQuantity = -1L;

            Throwable throwable = catchThrowable(() ->
                    new CartModel(1L, 1L, negativeQuantity )
            );

            assertThat(throwable)
                    .isInstanceOf(CoreException.class)
                    .extracting("errorType")
                    .isEqualTo(ErrorType.BAD_REQUEST);
        }
    }
}
