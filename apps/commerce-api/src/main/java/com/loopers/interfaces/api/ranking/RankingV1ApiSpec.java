package com.loopers.interfaces.api.ranking;


import com.loopers.application.ranking.RankingViewInfo;
import com.loopers.interfaces.api.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.ModelAttribute;

@Tag(name = "Ranking V1 API", description = "Ranking API 입니다.")
public interface RankingV1ApiSpec {

    @Operation(summary = "일간랭킹조회")
    ApiResponse<RankingViewInfo.ProductDailyList> getTodayRankingList(
            @Valid @ModelAttribute RankingV1Dto.SearchTodayRankingRequest searchTodayRanking
    );

    @Operation(summary = "주간랭킹조회")
    ApiResponse<RankingViewInfo.ProductWeeklyList> getWeeklyRankingList(
            @Valid @ModelAttribute RankingV1Dto.SearchWeeklyRankingRequest searchWeeklyRankingRequest
    );

    @Operation(summary = "월간랭킹조회")
    ApiResponse<RankingViewInfo.ProductMonthlyList> getMonthlyRankingList(
            @Valid @ModelAttribute RankingV1Dto.SearchMonthlyRankingRequest searchMonthlyRankingRequest
    );
}
