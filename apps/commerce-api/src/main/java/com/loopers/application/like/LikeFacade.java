package com.loopers.application.like;

import com.loopers.application.like.dto.LikeProductInfo;
import com.loopers.application.like.dto.LikeToggleInfo;
import com.loopers.domain.Like.LikeService;
import com.loopers.domain.Like.LikeToggleResult;
import com.loopers.domain.product.ProductModel;
import com.loopers.domain.product.ProductService;
import com.loopers.domain.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@RequiredArgsConstructor
@Component
public class LikeFacade {

    private final UserService userService;
    private final LikeService likeService;
    private final ProductService productService;

    @Transactional
    public LikeToggleInfo toggle(Long userId, Long productId){

        userService.checkExist(userId);
        ProductModel productModel = productService.checkSellable(productId);

        LikeToggleResult likeToggleResult = likeService.likeToggle(userId, productId);
        ProductModel result = productService.adjustLikeCount(productModel, likeToggleResult);

        return new LikeToggleInfo(likeToggleResult == LikeToggleResult.LIKED, result.getLikeCount());

    }

    public List<LikeProductInfo> getLikedProducts(Long userId){
        userService.checkExist(userId);

        List<Long> likedProductIds = likeService.getLikedProductIdsByUser(userId);

        if(likedProductIds.isEmpty()){
            return List.of();
        }

        List<ProductModel> productModels = productService.getListByIds(likedProductIds);

        return productModels.stream().map(LikeProductInfo::from).toList();
        
    }


}
