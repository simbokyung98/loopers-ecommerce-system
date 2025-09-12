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
    public ApiResponse<RankingViewInfo.ProductList> getTodayRankingList(RankingV1Dto.SearchTodayRankingRequest searchTodayRanking) {

        return ApiResponse.success(rankingViewFacade.getTodayTopProductsWithCache(searchTodayRanking.toCriteria()));

    }
}
