package com.loopers.domain.product;

import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Optional;


public interface ProductRepository {

    ProductModel save(ProductModel productModel);

    Optional<ProductModel> findById(Long id);

    Page<ProductModel> findAllByPaging(int page, int size, String orderType);

    List<ProductModel> findByIdIn(List<Long> ids);

}
