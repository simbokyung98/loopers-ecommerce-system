package com.loopers.domain.product;

import com.loopers.domain.brand.BrandRepository;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import com.loopers.utils.DatabaseCleanUp;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatException;
import static org.assertj.core.api.InstanceOfAssertFactories.type;
import static org.junit.jupiter.api.Assertions.assertAll;

@SpringBootTest
public class ProductServiceIntegrationTest {

    @Autowired
    ProductService productService;

    @Autowired
    ProductRepository productRepository;

    @Autowired
    BrandRepository brandRepository;

    @Autowired
    private DatabaseCleanUp databaseCleanUp;

    @AfterEach
    void tearDown() {
        databaseCleanUp.truncateAllTables();
    }

    @DisplayName("상품 정보를 조회할 때, ")
    @Nested
    class Get {
        @DisplayName("상품이 존재하지 않을 경우, NOT_FOUND 예외가 발생하며 실패한다")
        @Test
        void throwsNotFoundException_whenDoNotExist(){

            Long notExistProductId = 999L;


            assertThatException()
                    .isThrownBy(() -> productService.get(notExistProductId))
                    .isInstanceOf(CoreException.class)
                    .extracting("errorType", type(ErrorType.class))
                    .isEqualTo(ErrorType.NOT_FOUND);
        }

        @DisplayName("상품 id로 상품을 요청하면, 상품 정보를 반환한다.")
        @Test
        void returnProduct_whenProductExist(){

            //arrange
            Long BRAND_ID = 1L;

            String  name = "테스트 상품";
            Long stock = 0L;
            Long price = 0L;
            Long likeCount = 0L;
            ProductStatus status = ProductStatus.AVAILABLE;

            ProductModel productModel =
                    new ProductModel(name,stock, price, status, BRAND_ID);

            ProductModel response = productRepository.save(productModel);

            //act
            ProductModel result = productService.get(response.getId());

            //assert
            assertAll(
                    () -> assertThat(result).isNotNull(),
                    () -> assertThat(result.getName()).isEqualTo(name),
                    () -> assertThat(result.getStock()).isEqualTo(stock),
                    () -> assertThat(result.getPrice()).isEqualTo(price),
                    () -> assertThat(result.getStatus()).isEqualTo(status),
                    () -> assertThat(result.getLikeCount()).isEqualTo(likeCount)
            );

        }
    }
}
