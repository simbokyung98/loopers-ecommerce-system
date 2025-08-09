package com.loopers.infrastructure.product;

import com.loopers.domain.product.ProductModel;
import com.loopers.domain.product.ProductRepository;
import com.loopers.domain.product.ProductSnapshotResult;
import com.loopers.domain.product.ProductStatus;
import com.loopers.interfaces.api.product.OrderType;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.LockModeType;
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
    public List<ProductSnapshotResult> getProductsForSnapshot(List<Long> ids) {
        return jpaQueryFactory.select(
                Projections.constructor(ProductSnapshotResult.class,
                        productModel.id,
                        productModel.name,
                        productModel.price))
                .from(productModel)
                .where(productModel.id.in(ids), productModel.status.eq(ProductStatus.SELL))
                .orderBy(productModel.id.asc())
                .fetch();
    }

    @Override
    public Optional<ProductModel> getProduct(Long id) {
        return productJpaRepository.findById(id);
    }

    @Override
    public Optional<ProductModel> getProductForUpdate(Long id) {
        return jpaQueryFactory
                .selectFrom(productModel)
                .where(productModel.id.eq(id))
                .setLockMode(LockModeType.PESSIMISTIC_WRITE)
                .stream().findFirst();
    }

    @Override
    public void saveProducts(List<ProductModel> productModels) {

        productJpaRepository.saveAll(productModels);
        productJpaRepository.flush();
    }

    @Override
    public List<ProductModel> getSellableProductsByIdInForUpdate(List<Long> productIds) {
        return  jpaQueryFactory
                .selectFrom(productModel)
                .where(productModel.id.in(productIds),
                        productModel.status.eq(ProductStatus.SELL))
                .orderBy(productModel.id.asc()) // ✅ deadlock 방지
                .setLockMode(LockModeType.PESSIMISTIC_WRITE) // ✅ 락 설정
                .fetch();
    }

    @Override
    public List<ProductModel> getProductsByIdIn(List<Long> productIds) {
        return productJpaRepository.findByIdIn(productIds);
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
