package com.loopers.domain.coupon;

import com.loopers.domain.coupon.model.IssuedCouponModel;

import java.util.Optional;

public interface IssuedCouponRepository {
    IssuedCouponModel saveIssuedCoupon(IssuedCouponModel issuedCouponModel);

    Boolean isCouponIssuedToUser(Long userId, Long couponId);

    Optional<IssuedCouponModel> getIssuedCouponOfUser(Long userId, Long couponId);

    Optional<IssuedCouponModel> getIssuedCoupon(Long id);
}
