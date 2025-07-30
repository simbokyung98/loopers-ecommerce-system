package com.loopers.application.product;


import com.loopers.application.product.dto.ProductInfo;
import com.loopers.domain.brand.BrandModel;
import com.loopers.domain.brand.BrandService;
import com.loopers.domain.product.ProductModel;
import com.loopers.domain.product.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class ProductFacade {

    private final BrandService brandService;
    private final ProductService productService;

    public ProductInfo get(Long productId){

        ProductModel productModel = productService.get(productId);
        BrandModel brandModel = brandService.get(productModel.getId());

        return ProductInfo.from(productModel, brandModel);
    }



}
