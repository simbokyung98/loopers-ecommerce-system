package com.loopers.domain.brand;


import java.util.List;
import java.util.Optional;

public interface BrandRepository {

    BrandModel saveBrand(BrandModel brandModel);

    Optional<BrandModel> findById(Long id);
    List<BrandModel> findByIdIn(List<Long> id);
}
