package com.loopers.application.ranking;

import com.loopers.domain.brand.BrandModel;
import com.loopers.domain.brand.BrandService;
import com.loopers.domain.product.ProductModel;
import com.loopers.domain.product.ProductService;
import com.loopers.domain.ranking.ProductMetricMonthlyModel;
import com.loopers.domain.ranking.ProductMetricWeeklyModel;
import com.loopers.domain.ranking.RankingService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;

import java.time.LocalDate;
import java.util.*;

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
    @Mock
    private RankingService rankingService;

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

        // Redis mock 데이터
        Set<ZSetOperations.TypedTuple<String>> rows = new LinkedHashSet<>();
        rows.add(new MockTuple("1", 100.0));
        rows.add(new MockTuple("2", 90.0));
        rows.add(new MockTuple("3", 80.0));
        when(zSetOperations.reverseRangeWithScores(redisKey, 0, 2)).thenReturn(rows);

        // Product, Brand mock
        ProductModel product1 = mockProduct(1L, 10L);
        ProductModel product2 = mockProduct(2L, 20L);
        ProductModel product3 = mockProduct(3L, 30L);

        when(productService.getListByIds(List.of(1L, 2L, 3L)))
                .thenReturn(List.of(product1, product2, product3));

        when(brandService.getBrandMapByIds(List.of(10L, 20L, 30L)))
                .thenReturn(Map.of(
                        10L, mock(BrandModel.class),
                        20L, mock(BrandModel.class),
                        30L, mock(BrandModel.class)
                ));

        // when
        RankingViewInfo.ProductDailyList result = rankingViewFacade.getTodayTopProducts(criteria);

        // then
        assertThat(result.products()).hasSize(3);
        assertThat(result.products().get(0).rank()).isEqualTo(1);
        assertThat(result.products().get(1).rank()).isEqualTo(2);
        assertThat(result.products().get(2).rank()).isEqualTo(3);

        verify(productService).getListByIds(List.of(1L, 2L, 3L));
        verify(brandService).getBrandMapByIds(List.of(10L, 20L, 30L));
    }

    @Test
    void getWeeklyRankingWithPage_returnsProductsWithRank() {
        // given
        LocalDate start = LocalDate.now().minusDays(7);
        LocalDate end = LocalDate.now();
        RankingViewCriteria.SearchWeeklyRanking criteria =
                new RankingViewCriteria.SearchWeeklyRanking(1, 2, start, end);

        ProductMetricWeeklyModel metric1 = mockWeeklyMetric(1L, 1);
        ProductMetricWeeklyModel metric2 = mockWeeklyMetric(2L, 2);

        when(rankingService.getWeeklyListWithPage(criteria.toCommand()))
                .thenReturn(new PageImpl<>(List.of(metric1, metric2)));

        ProductModel product1 = mockProduct(1L, 10L);
        ProductModel product2 = mockProduct(2L, 20L);

        when(productService.getListByIds(List.of(1L, 2L)))
                .thenReturn(List.of(product1, product2));

        when(brandService.getBrandMapByIds(List.of(10L, 20L)))
                .thenReturn(Map.of(
                        10L, mock(BrandModel.class),
                        20L, mock(BrandModel.class)
                ));

        // when
        RankingViewInfo.ProductWeeklyList result = rankingViewFacade.getWeeklyRankingWithPage(criteria);

        // then
        assertThat(result.products()).hasSize(2);
        assertThat(result.products().get(0).rank()).isEqualTo(1);
        assertThat(result.products().get(1).rank()).isEqualTo(2);

        verify(productService).getListByIds(List.of(1L, 2L));
        verify(brandService).getBrandMapByIds(List.of(10L, 20L));
    }

    @Test
    void getMonthlyRankingWithPage_returnsProductsWithRank() {
        // given
        LocalDate start = LocalDate.now().minusMonths(1);
        LocalDate end = LocalDate.now();
        RankingViewCriteria.SearchMonthlyRanking criteria =
                new RankingViewCriteria.SearchMonthlyRanking(1, 2, start, end);

        ProductMetricMonthlyModel metric1 = mockMonthlyMetric(3L, 1);
        ProductMetricMonthlyModel metric2 = mockMonthlyMetric(4L, 2);

        when(rankingService.getMonthlyListWithPage(criteria.toCommand()))
                .thenReturn(new PageImpl<>(List.of(metric1, metric2)));

        ProductModel product3 = mockProduct(3L, 30L);
        ProductModel product4 = mockProduct(4L, 40L);

        when(productService.getListByIds(List.of(3L, 4L)))
                .thenReturn(List.of(product3, product4));

        when(brandService.getBrandMapByIds(List.of(30L, 40L)))
                .thenReturn(Map.of(
                        30L, mock(BrandModel.class),
                        40L, mock(BrandModel.class)
                ));

        // when
        RankingViewInfo.ProductMonthlyList result = rankingViewFacade.getMonthlyRankingWithPage(criteria);

        // then
        assertThat(result.products()).hasSize(2);
        assertThat(result.products().get(0).rank()).isEqualTo(1);
        assertThat(result.products().get(1).rank()).isEqualTo(2);

        verify(productService).getListByIds(List.of(3L, 4L));
        verify(brandService).getBrandMapByIds(List.of(30L, 40L));
    }

    // ===============================
    // 헬퍼 메서드 / Mock 클래스
    // ===============================

    private static ProductModel mockProduct(Long id, Long brandId) {
        ProductModel product = mock(ProductModel.class);
        when(product.getId()).thenReturn(id);
        when(product.getBrandId()).thenReturn(brandId);
        return product;
    }

    private static ProductMetricWeeklyModel mockWeeklyMetric(Long productId, int rank) {
        ProductMetricWeeklyModel metric = mock(ProductMetricWeeklyModel.class);
        when(metric.getProductId()).thenReturn(productId);
        when(metric.getRankValue()).thenReturn(rank);
        return metric;
    }

    private static ProductMetricMonthlyModel mockMonthlyMetric(Long productId, int rank) {
        ProductMetricMonthlyModel metric = mock(ProductMetricMonthlyModel.class);
        when(metric.getProductId()).thenReturn(productId);
        when(metric.getRankValue()).thenReturn(rank);
        return metric;
    }

    // TypedTuple mock 구현체
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
