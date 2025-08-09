package com.loopers.application.brand;


import com.loopers.domain.brand.BrandModel;

public class BrandInfo {

    public record Brand(
            Long id,
            String name
    ){
        public static Brand of(BrandModel brandModel){
            return new Brand(brandModel.getId(), brandModel.getName());
        }
    }
}
