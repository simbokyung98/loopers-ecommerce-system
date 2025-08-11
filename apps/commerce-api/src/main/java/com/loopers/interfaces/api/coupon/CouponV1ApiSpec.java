package com.loopers.interfaces.api.coupon;


import com.loopers.interfaces.api.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

@Tag(name = "Coupon V1 API", description = "Coupon API 입니다.")
public interface CouponV1ApiSpec {

    @Operation(summary = "쿠폰 발행")
    ApiResponse<CouponV1Dto.IssuedCouponResponse> issue(
            @RequestHeader("X-USER-ID") Long userid,
            @RequestBody CouponV1Dto.IssueCouponRequest request
    );
}
