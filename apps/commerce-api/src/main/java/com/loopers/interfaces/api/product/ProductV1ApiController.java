package com.loopers.interfaces.api.product;


import com.loopers.application.common.PageInfo;
import com.loopers.application.product.ProductFacade;
import com.loopers.application.product.dto.ProductInfo;
import com.loopers.interfaces.api.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/products")
public class ProductV1ApiController implements ProductV1ApiSpec {

    private final ProductFacade productFacade;

    @Override
    @GetMapping("/{productId}")
    public ApiResponse<ProductV1Dto.ProductResponse> getProduct(@PathVariable Long productId) {
        ProductInfo.Product product = productFacade.getProduct(productId);
        ProductV1Dto.ProductResponse productResponse = ProductV1Dto.ProductResponse.from(product);
        return ApiResponse.success(productResponse);
    }

    @Override
    @GetMapping
    public ApiResponse<ProductV1Dto.PageEnvelope<ProductV1Dto.ProductResponse>> getProductsWithPageAndSort(
            @ModelAttribute ProductV1Dto.SearchProductRequest searchProductRequest) {

        PageInfo.PageEnvelope<ProductInfo.Product> info = productFacade.getProductsWithPageAndSort(searchProductRequest.toCriteria());

        return ApiResponse.success(ProductV1Dto.PageEnvelope.from(info, ProductV1Dto.ProductResponse::from));
    }
}
