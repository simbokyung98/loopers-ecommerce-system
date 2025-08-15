package com.loopers.interfaces.api.product;


import com.loopers.interfaces.api.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;

@Tag(name = "Product V1 API", description = "Product API 입니다.")
public interface ProductV1ApiSpec {

    @Operation(summary = "상품 조회")
    ApiResponse<ProductV1Dto.ProductResponse> getProduct(
            @PathVariable Long productId
    );

    @Operation(summary = "상품 목록 조회")
    ApiResponse<ProductV1Dto.PageEnvelope<ProductV1Dto.ProductResponse>> getProductsWithPageAndSort(
            @Valid @ModelAttribute ProductV1Dto.SearchProductRequest searchProductRequest
    );
}
