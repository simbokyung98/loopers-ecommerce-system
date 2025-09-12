package com.loopers.application.ranking;

import java.time.LocalDate;

public class RankingViewCriteria {

    public record SearchTodayRanking(
            int page,
            int size,
            LocalDate date
    ){


    }
}
