package com.loopers.application.like;

import com.loopers.application.like.dto.LikeProductInfo;
import com.loopers.domain.Like.LikeService;
import com.loopers.domain.product.ProductModel;
import com.loopers.domain.product.ProductService;
import com.loopers.domain.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@RequiredArgsConstructor
@Component
public class LikeFacade {

    private final UserService userService;
    private final LikeService likeService;
    private final ProductService productService;

    public void like(Long userId, Long productId){

        userService.checkExistUser(userId);
        productService.checkExistProduct(productId);

        likeService.like(userId, productId);

    }


    public void dislike(Long userId, Long productId){

        userService.checkExistUser(userId);
        likeService.dislike(userId, productId);

    }

    public List<LikeProductInfo> getLikedProducts(Long userId){
        userService.checkExistUser(userId);

        List<Long> likedProductIds = likeService.getLikedProductIdsByUser(userId);

        if(likedProductIds.isEmpty()){
            return List.of();
        }

        List<ProductModel> productModels = productService.getListByIds(likedProductIds);

        return productModels.stream().map(LikeProductInfo::from).toList();
        
    }


}
