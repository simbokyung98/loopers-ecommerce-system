package com.loopers.domain.coupon;

import com.loopers.domain.coupon.model.IssuedCouponModel;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

public class IssuedCouponModelTest {

    @Test
    @DisplayName("생성 시 couponId, userId, issuedAt이 올바르게 설정된다")
    void initializeFieldsOnCreation() {
        // given
        Long couponId = 1L;
        Long userId = 100L;

        // when
        IssuedCouponModel issuedCoupon = new IssuedCouponModel(couponId, userId);

        // then
        assertThat(issuedCoupon.getCouponId()).isEqualTo(couponId);
        assertThat(issuedCoupon.getUserId()).isEqualTo(userId);
        assertThat(issuedCoupon.getIssuedAt()).isNotNull();
    }


    @Test
    @DisplayName("이미 사용된 쿠폰을 다시 사용하려고 하면 BAD_REQUEST 예외가 발생한다.")
    void throwBadRequest_whenAlreadyUsed() {

        IssuedCouponModel issuedCoupon = new IssuedCouponModel(1L, 100L);

        issuedCoupon.use();

        assertThatThrownBy(issuedCoupon::use)
                .isInstanceOf(CoreException.class)
                .extracting("errorType")
                .isEqualTo(ErrorType.BAD_REQUEST);
    }
}
