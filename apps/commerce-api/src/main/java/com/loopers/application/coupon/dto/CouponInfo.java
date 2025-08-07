package com.loopers.application.coupon.dto;


import com.loopers.domain.coupon.model.IssuedCouponModel;

import java.time.ZonedDateTime;

public class CouponInfo {

    public record IssuedCoupon(
            Long id,
            Long couponId,
            Long userId,
            ZonedDateTime issuedAt
    ){
      public static IssuedCoupon form(IssuedCouponModel issuedCouponModel){
          return new IssuedCoupon(
                  issuedCouponModel.getId(),
                  issuedCouponModel.getCouponId(),
                  issuedCouponModel.getUserId(),
                  issuedCouponModel.getIssuedAt()
          );
      }
    }
}
