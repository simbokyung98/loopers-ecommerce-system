package com.loopers.interfaces.api.like;


import com.loopers.interfaces.api.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.RequestHeader;

@Tag(name = "Like V1 API", description = "Like API 입니다.")
public interface LikeV1ApiSpec {

    @Operation(summary = "상품 좋아요 등록")
    ApiResponse<Object> like(
            @RequestHeader("X-USER-ID") Long userid,
            Long productId
    );

    @Operation(summary = "상품 좋아요 취소")
    ApiResponse<Object> dislike(
            @RequestHeader("X-USER-ID") Long userid,
            Long productId
    );



    @Operation(summary = "내가 좋아요 한 상품 목록 조회")
    ApiResponse<LikeV1Dto.LikeProductsResponse> getLikedProducts(
            @RequestHeader("X-USER-ID") Long userid
    );
}
