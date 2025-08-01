package com.loopers.infrastructure.product;

import com.loopers.domain.order.OrderStatus;
import com.loopers.domain.product.ProductModel;
import com.loopers.domain.product.ProductRepository;
import com.loopers.domain.product.ProductStatus;
import com.loopers.interfaces.api.product.OrderType;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

import static com.loopers.domain.product.QProductModel.productModel;

@RequiredArgsConstructor
@Repository
public class ProductRepositoryImpl implements ProductRepository {

    private final ProductJpaRepository productJpaRepository;
    private final JPAQueryFactory jpaQueryFactory;
    @Override
    public ProductModel saveProduct(ProductModel productModel) {
        return productJpaRepository.save(productModel);
    }

    @Override
    public Boolean existProduct(Long id) {
        return productJpaRepository.existsById(id);
    }

    @Override
    public Boolean existProductByStatus(Long id, ProductStatus productStatus) {
        return productJpaRepository.existsByStatus(productStatus);
    }

    @Override
    public Page<ProductModel> findAllByPaging(int page, int size, OrderType orderType) {
        PageRequest pageRequest = PageRequest.of(page, size);

        List<ProductModel> productModelList = jpaQueryFactory.selectFrom(productModel)
                .orderBy(order(orderType))
                .offset(pageRequest.getOffset())
                .limit(pageRequest.getPageSize())
                .fetch()
                .stream()
                .toList();

        Long totalCount = jpaQueryFactory.select(productModel.count())
                .from(productModel)
                .fetchOne();

        long total = totalCount != null ? totalCount : 0L;

        return new PageImpl<>(productModelList, pageRequest, total);
    }

    @Override
    public List<ProductModel> findByIdIn(List<Long> ids) {
        return productJpaRepository.findByIdIn(ids);
    }

    @Override
    public Optional<ProductModel> getProduct(Long id) {
        return productJpaRepository.findById(id);
    }

    @Override
    public void saveProducts(List<ProductModel> productModels) {
        productJpaRepository.saveAll(productModels);
    }


    private OrderSpecifier<?> order(OrderType orderType){
        return switch (orderType){
            case 오래된순 -> productModel.createdAt.asc();
            case 최신순 -> productModel.createdAt.desc();
            case 낮은가격순 -> productModel.price.asc();
            case 높은가격순 -> productModel.price.desc();
            case 낮은좋아요순 -> productModel.likeCount.asc();
            case 높은좋아요순 -> productModel.likeCount.desc();
        };
    }




}
