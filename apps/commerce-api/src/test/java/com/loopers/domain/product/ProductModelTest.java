package com.loopers.domain.product;


import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.*;

class ProductModelTest {

    @DisplayName("상품 생성 시,")
    @Nested
    class create{

        static final Long BRAND_ID = 1L;

        @DisplayName("상품의 이름이 null 이면, BAD_REQUEST 예외가 발생한다.")
        @Test
        void throwsBadRequestException_whenNameIsNull(){

            Long brandId = 1L;

            Throwable throwable = catchThrowable(() ->
                    new ProductModel(null,0L, 0L, ProductStatus.SELL, BRAND_ID));

            assertThat(throwable)
                    .isInstanceOf(CoreException.class)
                    .extracting("errorType")
                    .isEqualTo(ErrorType.BAD_REQUEST);
        }

        @DisplayName("상품의 재고가 null이거나 음수면, BAD_REQUEST 예외가 발생한다.")
        @NullSource
        @ValueSource(longs = {
                Long.MIN_VALUE, -1L, -10L, -1000L,-10000L,-200000L
        })
        @ParameterizedTest
        void throwsBadRequestException_whenStockNullOrNegative(Long stock){
            Throwable throwable = catchThrowable(() ->
                    new ProductModel("테스트 상품",stock, 0L, ProductStatus.SELL, BRAND_ID));

            assertThat(throwable)
                    .isInstanceOf(CoreException.class)
                    .extracting("errorType")
                    .isEqualTo(ErrorType.BAD_REQUEST);
        }

        @DisplayName("상품의 가격이 null이거나 음수면, BAD_REQUEST 예외가 발생한다.")
        @NullSource
        @ValueSource(longs = {
                Long.MIN_VALUE, -1L, -10L, -1000L,-10000L,-200000L
        })
        @ParameterizedTest
        void throwsBadRequestException_whenPriceNullOrNegative(Long price){
            Throwable throwable = catchThrowable(() ->
                    new ProductModel("테스트 상품",0L, price, ProductStatus.SELL, BRAND_ID));

            assertThat(throwable)
                    .isInstanceOf(CoreException.class)
                    .extracting("errorType")
                    .isEqualTo(ErrorType.BAD_REQUEST);
        }

        @DisplayName("상품의 상태가 null인 경우, BAD_REQUEST 예외가 발생한다.")
        @Test
        void throwsBadRequestException_whenStatusIsNull(){
            Throwable throwable = catchThrowable(() ->
                    new ProductModel("테스트 상품",0L, 0L, null, BRAND_ID));

            assertThat(throwable)
                    .isInstanceOf(CoreException.class)
                    .extracting("errorType")
                    .isEqualTo(ErrorType.BAD_REQUEST);
        }

        @DisplayName("상품의 브랜드 id가 null인 경우, BAD_REQUEST 예외가 발생한다.")
        @Test
        void throwsBadRequestException_whenBrandIdIsNull(){
            Throwable throwable = catchThrowable(() ->
                    new ProductModel("테스트 상품",0L, 0L, ProductStatus.SELL, null));

            assertThat(throwable)
                    .isInstanceOf(CoreException.class)
                    .extracting("errorType")
                    .isEqualTo(ErrorType.BAD_REQUEST);
        }

        @DisplayName("올바른 값으로 상품 생성 요청시, 신규 상품을 생성한다.")
        @Test
        void createNewProduct_whenRequestWithValidValues(){
            String  name = "테스트 상품";
            Long stock = 0L;
            Long price = 0L;
            ProductStatus status = ProductStatus.SELL;

            ProductModel productModel =
                    new ProductModel(name,stock, price, status, BRAND_ID);

            assertThat(productModel).isNotNull();
            assertThat(productModel.getName()).isEqualTo(name);
            assertThat(productModel.getStock()).isEqualTo(stock);
            assertThat(productModel.getPrice()).isEqualTo(price);
            assertThat(productModel.getStatus()).isEqualTo(status);
        }


    }
}
