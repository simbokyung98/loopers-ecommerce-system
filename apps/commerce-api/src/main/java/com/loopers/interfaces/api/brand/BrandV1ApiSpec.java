package com.loopers.interfaces.api.brand;


import com.loopers.interfaces.api.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.RequestParam;

@Tag(name = "Brand V1 API", description = "Brand API 입니다.")
public interface BrandV1ApiSpec {

    @Operation(summary = "브랜드 조회")
    ApiResponse<BrandV1Dto.BrandResponse> getBrand(
            @RequestParam Long brandId
    );
}
