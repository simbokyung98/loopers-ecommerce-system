package com.loopers.infrastructure.brand;


import com.loopers.domain.brand.BrandModel;
import com.loopers.domain.brand.BrandRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class BrandRepositoryImpl implements BrandRepository {

    private final BrandJpaRepository brandJpaRepository;
    @Override
    public BrandModel saveBrand(BrandModel brandModel) {
        return brandJpaRepository.save(brandModel);
    }

    @Override
    public Optional<BrandModel> findById(Long id) {
        return brandJpaRepository.findById(id);
    }

    @Override
    public List<BrandModel> findByIdIn(List<Long> ids) {
        return brandJpaRepository.findByIdIn(ids);
    }
}
