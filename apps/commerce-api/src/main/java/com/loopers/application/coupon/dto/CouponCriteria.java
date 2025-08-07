package com.loopers.application.coupon.dto;


import com.loopers.domain.coupon.CouponCommand;

public class CouponCriteria {


    public record Issue(
            Long userId,
            Long couponId
    ){

        public CouponCommand.Issue toCommand(){
            return new CouponCommand.Issue(userId, couponId);

        }
    }
}
