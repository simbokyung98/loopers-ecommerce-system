package com.loopers.interfaces.api.order;


import com.loopers.interfaces.api.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

@Tag(name = "Order V1 API", description = "Order API 입니다.")
public interface OrderV1ApiSpec {

    @Operation(summary = "주문 요청")
    ApiResponse<OrderV1Dto.Order> order(
            @RequestHeader("X-USER-ID") Long userid,
            @RequestBody OrderV1Dto.OrderRequest request
    );

    @Operation(summary = "유저의 주문 목록 조회")
    ApiResponse<OrderV1Dto.UserOrdersResponse> getOrdersByUserId(
            @RequestHeader("X-USER-ID") Long userid
    );
}
