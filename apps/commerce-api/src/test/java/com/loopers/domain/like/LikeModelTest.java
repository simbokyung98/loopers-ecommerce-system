package com.loopers.domain.like;

import com.loopers.domain.Like.LikeModel;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

class LikeModelTest {

    @DisplayName("좋아요 생성 시,")
    @Nested
    class create {

        @DisplayName("좋아요의 유저 아이디가 null 이면, BAD_REQUEST 예외가 발생한다.")
        @Test
        void throwsBadRequestException_whenUserIdIsNull(){

            Long productId = 1L;

            Throwable throwable = catchThrowable(() ->
                  new LikeModel(null, productId));

            assertThat(throwable)
                    .isInstanceOf(CoreException.class)
                    .extracting("errorType")
                    .isEqualTo(ErrorType.BAD_REQUEST);
        }

        @DisplayName("좋아요의 상품 아이디가 null 이면, BAD_REQUEST 예외가 발생한다.")
        @Test
        void throwsBadRequestException_whenProductIdIsNull(){

            Long userId = 1L;

            Throwable throwable = catchThrowable(() ->
                  new LikeModel(userId, null));

            assertThat(throwable)
                    .isInstanceOf(CoreException.class)
                    .extracting("errorType")
                    .isEqualTo(ErrorType.BAD_REQUEST);
        }
    }

}
