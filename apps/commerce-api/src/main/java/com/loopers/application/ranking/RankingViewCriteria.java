package com.loopers.application.ranking;

import com.loopers.domain.ranking.RankingCommand;

import java.time.LocalDate;

public class RankingViewCriteria {

    public record SearchTodayRanking(
            int page,
            int size,
            LocalDate date
    ){


    }

    public record SearchWeeklyRanking(
            int page,
            int size,
            LocalDate startDate,
            LocalDate endDate
    ){
        public RankingCommand.SearchWeeklyRanking toCommand(){
            return new RankingCommand.SearchWeeklyRanking(page, size, startDate, endDate);
        }

    }

    public record SearchMonthlyRanking(
            int page,
            int size,
            LocalDate startDate,
            LocalDate endDate
    ){
        public RankingCommand.SearchMonthlyRanking toCommand(){
            return new RankingCommand.SearchMonthlyRanking(page, size, startDate, endDate);
        }
    }

}
