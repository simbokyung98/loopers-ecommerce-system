package com.loopers.infrastructure.product;

import com.loopers.domain.product.ProductModel;
import com.loopers.domain.product.ProductStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductJpaRepository extends JpaRepository<ProductModel, Long> {

    List<ProductModel> findAllByBrandId(Long brandId);

    List<ProductModel> findByIdIn(List<Long> id);

    Boolean existsByStatus(ProductStatus productStatus);
}
