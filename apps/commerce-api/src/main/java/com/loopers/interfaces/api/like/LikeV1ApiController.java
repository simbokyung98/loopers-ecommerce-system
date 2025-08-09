package com.loopers.interfaces.api.like;

import com.loopers.application.like.LikeFacade;
import com.loopers.application.like.dto.LikeCriteria;
import com.loopers.application.like.dto.LikeInfo;
import com.loopers.interfaces.api.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/like")
public class LikeV1ApiController implements LikeV1ApiSpec {

    private final LikeFacade likeFacade;

    @Override
    @PostMapping("/products/{productId}")
    public ApiResponse<Object> like(@RequestHeader(value = "X-USER-ID") Long userid, @PathVariable Long productId) {
        LikeCriteria.Like criteria = LikeCriteria.Like.of(userid, productId);
        likeFacade.like(criteria);
        return ApiResponse.success();
    }

    @Override
    @DeleteMapping("/products/{productId}")
    public ApiResponse<Object> dislike(@RequestHeader(value = "X-USER-ID") Long userid, @PathVariable Long productId) {
        LikeCriteria.Dislike criteria = LikeCriteria.Dislike.of(userid, productId);
        likeFacade.dislike(criteria);
        return ApiResponse.success();
    }

    @Override
    @GetMapping("/products")
    public ApiResponse<LikeV1Dto.LikeProductsResponse> getLikedProducts(@RequestHeader(value = "X-USER-ID") Long userid) {
        LikeInfo.LikeProducts likeProducts = likeFacade.getLikedProducts(userid);
        return ApiResponse.success(LikeV1Dto.LikeProductsResponse.of(likeProducts));
    }
}
