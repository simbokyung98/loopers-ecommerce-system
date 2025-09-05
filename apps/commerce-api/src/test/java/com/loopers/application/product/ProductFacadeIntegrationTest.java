package com.loopers.application.product;


import com.loopers.application.common.PageInfo;
import com.loopers.application.like.LikeFacade;
import com.loopers.application.like.dto.LikeCriteria;
import com.loopers.application.product.dto.ProductCriteria;
import com.loopers.application.product.dto.ProductInfo;
import com.loopers.domain.brand.BrandModel;
import com.loopers.domain.brand.BrandRepository;
import com.loopers.domain.product.ProductModel;
import com.loopers.domain.product.ProductRepository;
import com.loopers.domain.product.ProductStatus;
import com.loopers.domain.user.UserModel;
import com.loopers.domain.user.UserRepository;
import com.loopers.interfaces.api.product.OrderType;
import com.loopers.utils.DatabaseCleanUp;
import com.loopers.utils.RedisCleanUp;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertAll;

@SpringBootTest
class ProductFacadeIntegrationTest {

    @Autowired
    private ProductFacade productFacade;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private BrandRepository brandRepository;

    @Autowired
    private LikeFacade likeFacade;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private DatabaseCleanUp databaseCleanUp;

    @Autowired
    private RedisCleanUp redisCleanUp;

    @MockitoBean
    private KafkaTemplate<Object, Object> kafkaTemplate;

    @AfterEach
    void tearDown() {
        databaseCleanUp.truncateAllTables();
        redisCleanUp.truncateAll();
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
                    () -> assertThat(productInfo.brandName()).isEqualTo(brand.getName())
            );
        }


    }
    @Test
    @DisplayName("상품 목록 조회 시, 좋아요 수와 브랜드명이 포함된 DTO 리스트가 반환된다")
    void getProductsWithPageAndSort_success() {

        BrandModel brand = brandRepository.saveBrand(new BrandModel("나이키"));
        ProductModel p1 = productRepository.saveProduct(new ProductModel("신발", 10L, 10000L, ProductStatus.SELL, brand.getId()));
        ProductModel p2 = productRepository.saveProduct(new ProductModel("옷", 5L, 20000L, ProductStatus.SELL, brand.getId()));
        UserModel user1 = userRepository.save(new UserModel("testId1", "M", "2024-01-01", "test@example.com"));
        UserModel user2 = userRepository.save(new UserModel("testId2", "M", "2024-01-01", "test@example.com"));
        UserModel user3 = userRepository.save(new UserModel("testId3", "M", "2024-01-01", "test@example.com"));


        likeFacade.like(LikeCriteria.Like.of(user1.getId(), p1.getId()));
        likeFacade.like(LikeCriteria.Like.of(user2.getId(), p1.getId()));
        likeFacade.like(LikeCriteria.Like.of(user3.getId(), p2.getId()));


        ProductCriteria.SearchProducts criteria = new ProductCriteria.SearchProducts(0, 10, OrderType.낮은가격순, null);

//        // act
//        PageInfo.PageEnvelope<ProductInfo.Product> result = productFacade.getProductsWithPageAndSort(criteria);
//
//        // assert
//        assertThat(result.content()).hasSize(2);
//        assertThat(result.content())
//                .anySatisfy(product -> {
//                    if (product.id().equals(p1.getId())) {
//                        assertThat(product.likeCount()).isEqualTo(2L);
//                        assertThat(product.brandName()).isEqualTo("나이키");
//                    }
//                    if (product.id().equals(p2.getId())) {
//                        assertThat(product.likeCount()).isEqualTo(1L);
//                    }
//                });
//
//        assertThat(result.meta().totalElements()).isEqualTo(2);
//    }

        // when & then (await으로 이벤트 처리 대기)
        await()
                .atMost(2, TimeUnit.SECONDS) // 2초 동안 반복 확인
                .untilAsserted(() -> {
                    PageInfo.PageEnvelope<ProductInfo.Product> result = productFacade.getProductsWithPageAndSort(criteria);

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

                    assertThat(result.meta().totalElements()).isEqualTo(2);
                });
    }
}
