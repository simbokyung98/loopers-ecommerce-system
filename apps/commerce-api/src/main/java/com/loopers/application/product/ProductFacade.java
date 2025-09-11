package com.loopers.application.product;


import com.loopers.application.common.PageInfo;
import com.loopers.application.product.dto.ProductCriteria;
import com.loopers.application.product.dto.ProductInfo;
import com.loopers.cache.ProductDetailCache;
import com.loopers.cache.ProductListCache;
import com.loopers.confg.kafka.KafkaMessage;
import com.loopers.domain.brand.BrandModel;
import com.loopers.domain.brand.BrandService;
import com.loopers.domain.product.ProductModel;
import com.loopers.domain.product.ProductService;
import com.loopers.domain.product.ProductViewedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
@Component
public class ProductFacade {

    private final BrandService brandService;
    private final ProductService productService;

    private final ProductListCache productListCache;
    private final ProductDetailCache productDetailCache;

    private final KafkaTemplate<Object, Object> kafkaTemplate;
    private final RedisTemplate<String, String> redisTemplate;


    @Transactional(readOnly = true)
    public ProductInfo.Product getProduct(Long productId) {
        ProductInfo.Product product =  productDetailCache.getOrLoad(productId, () -> {
            ProductModel productModel = productService.get(productId);
            BrandModel brand = brandService.getBrand(productModel.getBrandId()); // ← getBrandId() 사용

            String key = getKey(LocalDate.now());
            Long rank = redisTemplate.opsForZSet().reverseRank(key, productId.toString());
            rank = rank != null ? rank + 1 : null;
            return ProductInfo.Product.fromRank(productModel, brand, rank);
        });

        ProductViewedEvent event = new ProductViewedEvent(productId);
        KafkaMessage<ProductViewedEvent> message = KafkaMessage.from(event);
        kafkaTemplate.send("product.viewed.v1", String.valueOf(productId), message);

        return product;
    }


    @Transactional(readOnly = true)
    public PageInfo.PageEnvelope<ProductInfo.Product> getProductsWithPageAndSort(
            ProductCriteria.SearchProducts criteria
    ) {
        // 캐시 어사이드: 미스 시에만 아래 람다(DB → 매핑) 실행
        return productListCache.getOrLoad(criteria, () -> {
            // 1) DB 조회 (페이지네이션 + 정렬 + 브랜드 필터는 service/rep에서 처리)
            Page<ProductModel> productModels =
                    productService.getProductsWithPageAndSort(criteria.toCommand());

            // 2) 현재 페이지에 필요한 브랜드만 모아서 벌크 조회
            List<Long> brandIds = productModels.getContent().stream()
                    .map(ProductModel::getBrandId)
                    .distinct()
                    .toList();
            Map<Long, BrandModel> brandModelMap =
                    brandService.getBrandMapByIds(brandIds);

            // 3) 도메인 → 응답 DTO 매핑
            Page<ProductInfo.Product> products =
                    productModels.map(model ->
                            ProductInfo.Product.from(model, brandModelMap.get(model.getBrandId()))
                    );

            // 4) 표준 페이지 래퍼로 포장
            return PageInfo.PageEnvelope.from(products);
        });
    }

    private String getKey(LocalDate localDate) {
        return "rank:all:" + localDate;
    }




}
