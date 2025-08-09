package com.loopers.domain.product;

import com.loopers.domain.BaseEntity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @InjectMocks
    private ProductService productService;

    @Mock
    private ProductRepository productRepository;

    @DisplayName("상품 재고 차감 시,")
    @Nested
    class DeductStocks{

        @Test
        @DisplayName("상품 재고가 충분할 경우, 각 상품의 재고를 차감하고 저장한다.")
        void deductStocksAndSaveProducts_whenStockIsSufficient() {

            Long productId1 = 1L;
            Long productId2 = 2L;

            Long BRAND_ID = 1L;

            ProductModel product1 =
                    new ProductModel(
                            "테스트 상품",
                            10L,
                            0L,
                            ProductStatus.SELL, BRAND_ID);
            ProductModel product2 =
                    new ProductModel(
                            "루퍼스 상품",
                            5L,
                            0L,
                            ProductStatus.SELL, BRAND_ID);
            setId(product1, productId1);
            setId(product2, productId2);

            List<ProductCommand.ProductQuantity> quantities = List.of(
                    new ProductCommand.ProductQuantity(productId1, 3L),
                    new ProductCommand.ProductQuantity(productId2, 2L)
            );
            ProductCommand.DeductStocks command = new ProductCommand.DeductStocks(quantities);

            when(productRepository.getProductsByIdIn(List.of(productId1, productId2)))
                    .thenReturn(List.of(product1, product2));

            // act
            productService.deductStocks(command);

            // assert
            assertThat(product1.getStock()).isEqualTo(7); // 10 - 3
            assertThat(product2.getStock()).isEqualTo(3); // 5 - 2

        }


        private static void setId(ProductModel product, Long id) {
            try {
                Field idField = BaseEntity.class.getDeclaredField("id");
                idField.setAccessible(true);
                idField.set(product, id);
            } catch (Exception e) {
                throw new RuntimeException("테스트용 id 설정 실패", e);
            }
        }

    }
}
