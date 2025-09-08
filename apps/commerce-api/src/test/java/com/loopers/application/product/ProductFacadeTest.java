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
import com.loopers.domain.product.ProductStatus;
import com.loopers.domain.product.ProductViewedEvent;
import com.loopers.interfaces.api.product.OrderType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.kafka.core.KafkaTemplate;

import java.util.Collections;
import java.util.Map;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
class ProductFacadeTest {

    @InjectMocks
    private ProductFacade productFacade;

    @Mock private BrandService brandService;
    @Mock private ProductService productService;

    // 캐시 2종(Mock)
    @Mock private ProductListCache productListCache;
    @Mock private ProductDetailCache productDetailCache;

    @Mock
    private KafkaTemplate<Object, Object> kafkaTemplate;



    private ProductCriteria.SearchProducts latestRequest() {
        return new ProductCriteria.SearchProducts(0, 20, OrderType.최신순, null);
    }
    private Page<ProductModel> emptyPage() {
        return new PageImpl<>(Collections.emptyList(), PageRequest.of(0, 20), 0);
    }
    private PageInfo.PageEnvelope<ProductInfo.Product> envelope() {
        PageInfo.PageMeta meta = new PageInfo.PageMeta(0, 20, 0L, 0);
        return PageInfo.PageEnvelope.of(Collections.emptyList(), meta);
    }


    @Nested
    @DisplayName("상품 목록")
    class ListTests {


        @Test
        @DisplayName("동일 조건 재요청 시, 추가 데이터 조회를 유발하지 않는다")
        void sameCondition_secondRequest_doesNotCallDataSources() {
            ProductCriteria.SearchProducts criteria = latestRequest();

            when(productListCache.getOrLoad(eq(criteria), any())).thenReturn(envelope());

            //act
            PageInfo.PageEnvelope<ProductInfo.Product> res =
                    productFacade.getProductsWithPageAndSort(criteria);

            //assert
            assertNotNull(res);
            verify(productListCache, times(1)).getOrLoad(eq(criteria), any());
            verifyNoInteractions(productService, brandService);
        }

        @Test
        @DisplayName("초기 조회 시, 서비스에서 데이터를 구성해 반환한다")
        void list_initialRequest_buildsFromServicesOnce() {
            ProductCriteria.SearchProducts criteria = latestRequest();


            when(productListCache.getOrLoad(eq(criteria), any()))
                    .thenAnswer(inv -> {
                        @SuppressWarnings("unchecked")
                        Supplier<PageInfo.PageEnvelope<ProductInfo.Product>> supplier = inv.getArgument(1);
                        return supplier.get();
                    });

            when(productService.getProductsWithPageAndSort(any())).thenReturn(emptyPage());
            when(brandService.getBrandMapByIds(anyList())).thenReturn(Map.of());

            //act
            PageInfo.PageEnvelope<ProductInfo.Product> res =
                    productFacade.getProductsWithPageAndSort(criteria);

            //assert
            assertNotNull(res);
            verify(productService, times(1)).getProductsWithPageAndSort(any());
            verify(brandService, times(1)).getBrandMapByIds(anyList());
        }
    }


    @Nested
    @DisplayName("상품 상세")
    class DetailTests {

        @Test
        @DisplayName("동일 항목 재요청 시, 불필요한 추가 조회를 하지 않는다")
        void detail_sameItem_repeatedRequest_avoidsExtraFetch() {
            Long id = 100L;
            ProductInfo.Product dto = new ProductInfo.Product(id, "테스트 상품", 100L, 1000L, ProductStatus.SELL, 1L, 10L, "테스트 브랜드");

            when(productDetailCache.getOrLoad(eq(id), any()))
                    .thenReturn(dto);

            //act
            ProductInfo.Product res = productFacade.getProduct(id);

            //assert
            assertThat(res).isSameAs(dto);
            verify(productDetailCache).getOrLoad(eq(id), any());
            verifyNoInteractions(productService, brandService);
        }

        @Test
        @DisplayName("초기 조회 시, 상품/브랜드 정보를 조합해 상세를 반환한다")
        void detail_initialRequest_fetchesProductAndBrand() {
            Long id = 101L;
            ProductInfo.Product dto = new ProductInfo.Product(id, "테스트 상품", 100L, 1000L, ProductStatus.SELL, 1L, 10L, "테스트 브랜드");


            when(productDetailCache.getOrLoad(eq(id), any()))
                    .thenAnswer(inv -> {
                        @SuppressWarnings("unchecked")
                        Supplier<ProductInfo.Product> supplier = inv.getArgument(1);

                        // supplier 내부에서 호출되는 서비스 동작을 스텁
                        ProductModel product = mock(ProductModel.class);
                        when(productService.get(id)).thenReturn(product);
                        when(product.getBrandId()).thenReturn(7L);

                        BrandModel brand = mock(BrandModel.class);
                        when(brandService.getBrand(7L)).thenReturn(brand);

                        // 실제 로더 실행
                        supplier.get();

                        return dto;
                    });

            //act
            ProductInfo.Product res = productFacade.getProduct(id);

            //assert
            assertThat(res).isSameAs(dto);
            verify(productDetailCache).getOrLoad(eq(id), any());
            verify(productService).get(id);
            verify(brandService).getBrand(7L);
        }

        @Test
        @DisplayName("상품 조회 시:  상품 반환 + ProductViewedEvent 발행")
        void getProduct_shouldReturnProductAndPublishEvent() {
            // given
            Long productId = 100L;
            ProductInfo.Product product = new ProductInfo.Product(productId, "테스트 상품", 100L, 1000L, ProductStatus.SELL, 1L, 10L, "테스트 브랜드");
            when(productDetailCache.getOrLoad(eq(productId), any())).thenReturn(product);

            // when
            ProductInfo.Product result = productFacade.getProduct(productId);

            // then
            assertThat(result).isEqualTo(product);

            // Kafka 이벤트 발행 검증
            verify(kafkaTemplate).send(
                    eq("product.viewed.v1"),
                    eq(productId.toString()),
                    argThat(message -> {
                        if (!(message instanceof KafkaMessage<?> kafkaMessage)) return false;
                        return kafkaMessage.payload() instanceof ProductViewedEvent event
                                && event.productId().equals(productId);
                    })
            );
        }
    }
}
