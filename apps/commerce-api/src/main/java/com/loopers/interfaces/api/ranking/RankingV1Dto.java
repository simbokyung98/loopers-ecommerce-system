package com.loopers.interfaces.api.ranking;


import com.loopers.application.ranking.RankingViewCriteria;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class RankingV1Dto {

    public record SearchTodayRankingRequest(
            @NotNull
            int page,
            @NotNull
            int size,
            @NotNull
            String date
    ){
        public RankingViewCriteria.SearchTodayRanking toCriteria(){
            LocalDate localDate = LocalDate.parse(date, DateTimeFormatter.ofPattern("yyyyMMdd"));
            return new RankingViewCriteria.SearchTodayRanking(page, size, localDate);
        }
    }



}
