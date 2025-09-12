package com.loopers.application.ranking;

import com.loopers.domain.brand.BrandModel;
import com.loopers.domain.brand.BrandService;
import com.loopers.domain.product.ProductModel;
import com.loopers.domain.product.ProductService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;

import java.time.LocalDate;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RankingViewFacadeTest {

    @Mock
    private RedisTemplate<String, String> redisTemplate;
    @Mock
    private ProductService productService;
    @Mock
    private BrandService brandService;
    @Mock
    private ZSetOperations<String, String> zSetOperations;

    @InjectMocks
    private RankingViewFacade rankingViewFacade;

    @Test
    void getTodayTopProducts_returnsProductsWithRank() {
        // given
        LocalDate today = LocalDate.now();
        RankingViewCriteria.SearchTodayRanking criteria =
                new RankingViewCriteria.SearchTodayRanking(1, 3, today);

        String redisKey = "rank:all:" + today;

        when(redisTemplate.opsForZSet()).thenReturn(zSetOperations);

        // Redis mock 데이터 (id=1,2,3)
        Set<ZSetOperations.TypedTuple<String>> rows = new LinkedHashSet<>();
        rows.add(new MockTuple("1", 100.0));
        rows.add(new MockTuple("2", 90.0));
        rows.add(new MockTuple("3", 80.0));
        when(zSetOperations.reverseRangeWithScores(redisKey, 0, 2))
                .thenReturn(rows);

        // Product, Brand mock
        ProductModel product1 = mock(ProductModel.class);
        when(product1.getId()).thenReturn(1L);
        when(product1.getBrandId()).thenReturn(10L);

        ProductModel product2 = mock(ProductModel.class);
        when(product2.getId()).thenReturn(2L);
        when(product2.getBrandId()).thenReturn(20L);

        ProductModel product3 = mock(ProductModel.class);
        when(product3.getId()).thenReturn(3L);
        when(product3.getBrandId()).thenReturn(30L);

        when(productService.getListByIds(List.of(1L, 2L, 3L)))
                .thenReturn(List.of(product1, product2, product3));

        when(brandService.getBrandMapByIds(List.of(10L, 20L, 30L)))
                .thenReturn(Map.of(
                        10L, mock(BrandModel.class),
                        20L, mock(BrandModel.class),
                        30L, mock(BrandModel.class)
                ));

        // when
        RankingViewInfo.ProductList result = rankingViewFacade.getTodayTopProducts(criteria);

        // then
        assertThat(result.products()).hasSize(3);
        assertThat(result.products().get(0).rank()).isEqualTo(1);
        assertThat(result.products().get(1).rank()).isEqualTo(2);
        assertThat(result.products().get(2).rank()).isEqualTo(3);

        verify(productService).getListByIds(List.of(1L, 2L, 3L));
        verify(brandService).getBrandMapByIds(List.of(10L, 20L, 30L));
    }

    // 간단한 TypedTuple mock 구현체
    static class MockTuple implements ZSetOperations.TypedTuple<String> {
        private final String value;
        private final Double score;
        MockTuple(String value, Double score) {
            this.value = value;
            this.score = score;
        }
        @Override public String getValue() { return value; }
        @Override public Double getScore() { return score; }
        @Override public int compareTo(ZSetOperations.TypedTuple<String> o) { return 0; }
    }
}
