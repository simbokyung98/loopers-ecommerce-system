package com.loopers.domain.coupon;

import com.loopers.domain.coupon.model.CouponModel;

import java.util.Optional;

public interface CouponRepository {
    Optional<CouponModel> findCouponByIdForUpdate(Long couponId);

    CouponModel saveCoupon(CouponModel couponModel);

    Optional<CouponModel> getCoupon(Long couponId);
}
