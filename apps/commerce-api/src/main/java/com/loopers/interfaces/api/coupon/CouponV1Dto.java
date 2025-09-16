package com.loopers.interfaces.api.coupon;


import com.loopers.application.coupon.dto.CouponCriteria;
import com.loopers.application.coupon.dto.CouponInfo;
import jakarta.validation.constraints.NotNull;

import java.time.ZonedDateTime;

public class CouponV1Dto {
    public record IssueCouponRequest(
            @NotNull
            Long couponId,
            String name,
            Long issuedCount,
            Long issued_limit,
            ZonedDateTime expires_at
    ){
        public CouponCriteria.Issue toCriteria(Long userId){
            return new CouponCriteria.Issue(userId, couponId);
        }
    }

    public record IssuedCouponResponse(
            Long id,
            Long couponId,
            Long userId,
            ZonedDateTime issuedAt
    ){
        public static IssuedCouponResponse from(CouponInfo.IssuedCoupon issuedCoupon){
            return new IssuedCouponResponse(
                    issuedCoupon.id(),
                    issuedCoupon.couponId(),
                    issuedCoupon.userId(),
                    issuedCoupon.issuedAt()
            );
        }
    }
}
