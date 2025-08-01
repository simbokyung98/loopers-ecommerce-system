package com.loopers.application.product;


import com.loopers.application.product.dto.ProductCriteria;
import com.loopers.application.product.dto.ProductInfo;
import com.loopers.domain.Like.LikeModel;
import com.loopers.domain.Like.LikeRepository;
import com.loopers.domain.brand.BrandModel;
import com.loopers.domain.brand.BrandRepository;
import com.loopers.domain.product.ProductModel;
import com.loopers.domain.product.ProductRepository;
import com.loopers.domain.product.ProductStatus;
import com.loopers.interfaces.api.product.OrderType;
import com.loopers.utils.DatabaseCleanUp;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class ProductFacadeIntegrationTest {

    @Autowired
    private ProductFacade productFacade;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private BrandRepository brandRepository;

    @Autowired
    private LikeRepository likeRepository;

    @Autowired
    private DatabaseCleanUp databaseCleanUp;

    @AfterEach
    void tearDown() {
        databaseCleanUp.truncateAllTables();
    }

    @DisplayName("상품 정보를 조회할 떄 , ")
    @Nested
    class Get {

        @DisplayName("상품 아이디로 상품 정보를 조회하면, 상품과 그 상품의 브랜드 정보와 좋아요 수를 반환한다.")
        @Test
        void returnProductWithBrand_whenProductIdExist(){

            BrandModel brandModel = new BrandModel("테스트 브랜드");
            BrandModel brand =
                    brandRepository.saveBrand(brandModel);
            ProductModel productModel =
                    new ProductModel(
                            "테스트 상품",
                            0L,
                            0L,
                            ProductStatus.SELL, brand.getId());
            ProductModel product = productRepository.saveProduct(productModel);

            ProductInfo.Product productInfo = productFacade.getProduct(product.getId());

            assertAll(
                    () -> assertThat(productInfo).isNotNull(),
                    () -> assertThat(productInfo.brandName()).isEqualTo(brand.getName()),
                    () -> assertThat(productInfo.likeCount()).isEqualTo(0L)
            );
        }

        @Test
        @DisplayName("상품 목록 조회 시, 좋아요 수와 브랜드명이 포함된 DTO 리스트가 반환된다")
        void getProductsWithPageAndSort_success() {

            BrandModel brand = brandRepository.saveBrand(new BrandModel("나이키"));
            ProductModel p1 = productRepository.saveProduct(new ProductModel("신발", 10L, 10000L, ProductStatus.SELL, brand.getId()));
            ProductModel p2 = productRepository.saveProduct(new ProductModel("옷", 5L, 20000L, ProductStatus.SELL, brand.getId()));

            likeRepository.save(new LikeModel(1L, p1.getId()));
            likeRepository.save(new LikeModel(2L, p1.getId()));
            likeRepository.save(new LikeModel(3L, p2.getId()));

            ProductCriteria.SearchProducts criteria = new ProductCriteria.SearchProducts(0, 10, OrderType.낮은가격순);

            // act
            ProductInfo.PageResponse<ProductInfo.Product> result = productFacade.getProductsWithPageAndSort(criteria);

            // assert
            assertThat(result.content()).hasSize(2);
            assertThat(result.content())
                    .anySatisfy(product -> {
                        if (product.id().equals(p1.getId())) {
                            assertThat(product.likeCount()).isEqualTo(2L);
                            assertThat(product.brandName()).isEqualTo("나이키");
                        }
                        if (product.id().equals(p2.getId())) {
                            assertThat(product.likeCount()).isEqualTo(1L);
                        }
                    });

            assertThat(result.totalElements()).isEqualTo(2);
        }
    }


}
