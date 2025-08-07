package com.loopers.application.coupon;

import com.loopers.application.coupon.dto.CouponCriteria;
import com.loopers.application.coupon.dto.CouponInfo;
import com.loopers.domain.coupon.CouponService;
import com.loopers.domain.coupon.model.IssuedCouponModel;
import com.loopers.domain.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Component
public class CouponFacade {

    private final UserService userService;
    private final CouponService couponService;


    @Transactional
    public CouponInfo.IssuedCoupon issue(CouponCriteria.Issue criteria){
        userService.checkExistUser(criteria.userId());
        couponService.validateCouponNotIssued(criteria.toCommand());

        IssuedCouponModel issuedCouponModel = couponService.issue(criteria.toCommand());

        return CouponInfo.IssuedCoupon.form(issuedCouponModel);
    }
}
