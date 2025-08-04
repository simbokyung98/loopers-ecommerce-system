package com.loopers.domain.product;

import com.loopers.interfaces.api.product.OrderType;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Optional;


public interface ProductRepository {

    ProductModel saveProduct(ProductModel productModel);

    Boolean existProduct(Long id);

    Boolean existProductByStatus(Long id, ProductStatus productStatus);

    Page<ProductModel> findAllByPaging(int page, int size, OrderType orderType);

    List<ProductModel> findByIdIn(List<Long> ids);

    Optional<ProductModel> getProduct(Long id);

    void saveProducts(List<ProductModel> productModels);

    List<ProductModel> findByIdInWithPessimisticLock(List<Long> productIds);

}
