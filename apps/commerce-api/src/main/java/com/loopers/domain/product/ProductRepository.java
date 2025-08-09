package com.loopers.domain.product;

import com.loopers.interfaces.api.product.OrderType;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Optional;


public interface ProductRepository {

    ProductModel saveProduct(ProductModel productModel);

    Boolean existProduct(Long id);

    Page<ProductModel> findAllByPaging(int page, int size, OrderType orderType);

    List<ProductSnapshotResult> getProductsForSnapshot(List<Long> ids);

    Optional<ProductModel> getProduct(Long id);

    Optional<ProductModel> getProductForUpdate(Long id);

    void saveProducts(List<ProductModel> productModels);

    List<ProductModel> getSellableProductsByIdInForUpdate(List<Long> productIds);
    List<ProductModel> getProductsByIdIn(List<Long> productIds);

}
