package com.loopers.interfaces.api.ranking;

import com.loopers.application.ranking.RankingViewFacade;
import com.loopers.application.ranking.RankingViewInfo;
import com.loopers.interfaces.api.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/ranking")
public class RankingV1ApiController implements RankingV1ApiSpec {

    private final RankingViewFacade rankingViewFacade;
    @Override
    public ApiResponse<RankingViewInfo.ProductDailyList> getTodayRankingList(RankingV1Dto.SearchTodayRankingRequest searchTodayRanking) {

        return ApiResponse.success(rankingViewFacade.getTodayTopProductsWithCache(searchTodayRanking.toCriteria()));

    }

    @Override
    public ApiResponse<RankingViewInfo.ProductWeeklyList> getWeeklyRankingList(RankingV1Dto.SearchWeeklyRankingRequest searchWeeklyRankingRequest) {
        return ApiResponse.success(rankingViewFacade.getWeeklyRankingWithPage(searchWeeklyRankingRequest.toCriteria()));
    }

    @Override
    public ApiResponse<RankingViewInfo.ProductMonthlyList> getMonthlyRankingList(RankingV1Dto.SearchMonthlyRankingRequest searchMonthlyRankingRequest) {
        return ApiResponse.success(rankingViewFacade.getMonthlyRankingWithPage(searchMonthlyRankingRequest.toCriteria()));
    }
}
