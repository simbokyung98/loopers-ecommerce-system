package com.loopers.application.product;


import com.loopers.application.product.dto.ProductCriteria;
import com.loopers.application.product.dto.ProductInfo;
import com.loopers.domain.Like.LikeService;
import com.loopers.domain.brand.BrandModel;
import com.loopers.domain.brand.BrandService;
import com.loopers.domain.product.ProductModel;
import com.loopers.domain.product.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
@Component
public class ProductFacade {

    private final BrandService brandService;
    private final ProductService productService;
    private final LikeService likeService;

    public ProductInfo.Product getProduct(Long productId){

        ProductModel productModel = productService.get(productId);
        BrandModel brandModel = brandService.get(productModel.getId());
        Long likeCount = likeService.countLikeByProductId(productId);


        return ProductInfo.Product.from(productModel, brandModel, likeCount);
    }

    @Transactional(readOnly = true)
    public ProductInfo.PageResponse<ProductInfo.Product> getProductsWithPageAndSort(ProductCriteria.SearchProducts criteria){

        Page<ProductModel> productModels = productService.getProductsWithPageAndSort(criteria.page(),criteria.size(),criteria.orderType());

        List<Long> productIds =productModels.map(ProductModel::getId).stream().toList();
        List<Long> brandIds =productModels.map(ProductModel::getBrandId).stream().distinct().toList();

        Map<Long, Long> likeCountMap = likeService.countLikeByProductIds(productIds);

        Map<Long, BrandModel> brandModelMap = brandService.getBrandMapByIds(brandIds);

        Page<ProductInfo.Product> products =
                productModels.map(model -> ProductInfo.Product.from(model, brandModelMap.get(model.getBrandId()) ,likeCountMap.getOrDefault(model.getId(), 0L)));

        return ProductInfo.PageResponse.from(products);
    }




}
