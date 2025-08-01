package com.loopers.application.product;


import com.loopers.application.product.dto.ProductInfo;
import com.loopers.domain.brand.BrandModel;
import com.loopers.domain.brand.BrandRepository;
import com.loopers.domain.product.ProductModel;
import com.loopers.domain.product.ProductRepository;
import com.loopers.domain.product.ProductStatus;
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
    private DatabaseCleanUp databaseCleanUp;

    @AfterEach
    void tearDown() {
        databaseCleanUp.truncateAllTables();
    }

    @DisplayName("상품 정보를 조회할 떄 , ")
    @Nested
    class Get {

        @DisplayName("상품 아이디로 상품 정보를 조회하면, 상품과 그 상품의 브랜드 정보를 반환한다.")
        @Test
        void returnProductWithBrand_whenProductIdExist(){

            BrandModel brandModel = new BrandModel("테스트 브랜드");
            BrandModel brand =
                    brandRepository.save(brandModel);
            ProductModel productModel =
                    new ProductModel(
                            "테스트 상품",
                            0L,
                            0L,
                            ProductStatus.SELL, brand.getId());
            ProductModel product = productRepository.saveProduct(productModel);

            ProductInfo productInfo = productFacade.get(product.getId());

            assertAll(
                    () -> assertThat(productInfo).isNotNull(),
                    () -> assertThat(productInfo.brandName()).isEqualTo(brand.getName())
            );
        }
    }


}
