package com.loopers.domain.brand;


import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class BrandService {

    private final BrandRepository brandRepository;

    @Transactional(readOnly = true)
    public BrandModel get(Long id){
        Optional<BrandModel> optionalProductModel = brandRepository.findById(id);

        if(optionalProductModel.isEmpty()){
            throw new CoreException(ErrorType.NOT_FOUND, "상품 정보를 찾을 수 없습니다.");
        }
        return optionalProductModel.get();
    }

    public BrandModel save(String name){

        BrandModel brandModel = new BrandModel(name);
        return brandRepository.saveBrand(brandModel);
    }

    public Map<Long, BrandModel> getBrandMapByIds(List<Long> brandIds) {
        List<BrandModel> brands = brandRepository.findByIdIn(brandIds);
        return brands.stream().collect(Collectors.toMap(BrandModel::getId, Function.identity()));
    }

}
