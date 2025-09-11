package com.loopers.interfaces.api.coupon;


import com.loopers.application.coupon.CouponFacade;
import com.loopers.application.coupon.dto.CouponCriteria;
import com.loopers.application.coupon.dto.CouponInfo;
import com.loopers.interfaces.api.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/coupons")
public class CouponV1ApiController implements  CouponV1ApiSpec{

    private final CouponFacade couponFacade;
    @Override
    @PostMapping("/issue")
    public ApiResponse<Object> issue(@RequestHeader("X-USER-ID") Long userid,
                                                               @RequestBody CouponV1Dto.IssueCouponRequest request) {
        CouponCriteria.Issue criteria = request.toCriteria(userid);
        CouponInfo.IssuedCoupon issuedCoupon = couponFacade.issue(criteria);
        CouponV1Dto.IssuedCouponResponse response = CouponV1Dto.IssuedCouponResponse.from(issuedCoupon);
        return ApiResponse.success(response);
    }
}
