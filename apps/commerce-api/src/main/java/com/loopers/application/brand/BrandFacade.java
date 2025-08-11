package com.loopers.application.brand;

import com.loopers.domain.brand.BrandModel;
import com.loopers.domain.brand.BrandService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class BrandFacade {

    private final BrandService brandService;


    public BrandInfo.Brand getBrand(Long id){
        BrandModel brandModel = brandService.getBrand(id);

        return BrandInfo.Brand.of(brandModel);
    }
}
