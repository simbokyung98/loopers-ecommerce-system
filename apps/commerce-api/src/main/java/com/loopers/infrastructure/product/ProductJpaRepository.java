package com.loopers.infrastructure.product;

import com.loopers.domain.product.ProductModel;
import com.loopers.domain.product.ProductStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;

import java.util.List;

public interface ProductJpaRepository extends JpaRepository<ProductModel, Long> {

    List<ProductModel> findAllByBrandId(Long brandId);

    List<ProductModel> findByIdIn(List<Long> id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    List<ProductModel> findAllByIdIn(List<Long> id);

    Boolean existsByStatus(ProductStatus productStatus);
}
