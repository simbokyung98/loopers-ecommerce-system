package com.loopers.interfaces.api.like;

import com.loopers.application.like.LikeFacade;
import com.loopers.application.like.dto.LikeCriteria;
import com.loopers.application.like.dto.LikeInfo;
import com.loopers.interfaces.api.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/likes")
public class LikeV1ApiController implements LikeV1ApiSpec {

    private final LikeFacade likeFacade;

    @Override
    @PostMapping("like")
    public ApiResponse<Object> like(@RequestHeader(value = "X-USER-ID") Long userid, @RequestBody LikeV1Dto.LikeRequest likeRequest) {
        LikeCriteria.Like criteria = likeRequest.toCriteria(userid);
        likeFacade.like(criteria);
        return ApiResponse.success();
    }

    @Override
    @PostMapping("/dislike")
    public ApiResponse<Object> dislike(@RequestHeader(value = "X-USER-ID") Long userid, @RequestBody LikeV1Dto.DislikeRequest likeRequest) {
        LikeCriteria.Dislike criteria = likeRequest.toCriteria(userid);
        likeFacade.dislike(criteria);
        return ApiResponse.success();
    }

    @Override
    @GetMapping
    public ApiResponse<LikeV1Dto.LikeProductsResponse> getLikedProducts(@RequestHeader(value = "X-USER-ID") Long userid) {
        LikeInfo.LikeProducts likeProducts = likeFacade.getLikedProducts(userid);
        return ApiResponse.success(LikeV1Dto.LikeProductsResponse.of(likeProducts));
    }
}
