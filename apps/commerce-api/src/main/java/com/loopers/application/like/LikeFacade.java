package com.loopers.application.like;

import com.loopers.application.like.dto.LikeCriteria;
import com.loopers.application.like.dto.LikeInfo;
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

    public void like(LikeCriteria.Like criteria){

        userService.checkExistUser(criteria.userId());
        productService.checkExistProduct(criteria.productId());

        likeService.like(criteria.userId(), criteria.productId());

    }


    public void dislike(LikeCriteria.Dislike command){

        userService.checkExistUser(command.userId());
        likeService.dislike(command.userId(), command.productId());

    }

    public LikeInfo.LikeProducts getLikedProducts(Long userId){
        userService.checkExistUser(userId);

        List<Long> likedProductIds = likeService.getLikedProductIdsByUser(userId);

        if(likedProductIds.isEmpty()){
            return LikeInfo.LikeProducts.of(userId, List.of());
        }

        List<ProductModel> productModels = productService.getListByIds(likedProductIds);

        return LikeInfo.LikeProducts.of(userId, productModels);
        
    }


}
