package com.loopers.interfaces.api.ranking;


import com.loopers.application.ranking.RankingViewCriteria;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

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


    public record SearchWeeklyRankingRequest(
            @NotNull
            int page,
            @NotNull
            int size,
            @NotNull
            String startDate,
            @NotNull
            String endDate

            ){
        public RankingViewCriteria.SearchWeeklyRanking toCriteria(){
            LocalDate startLocalDate = LocalDate.parse(startDate, DateTimeFormatter.ofPattern("yyyyMMdd"));

            LocalDate endLocalDate = LocalDate.parse(endDate, DateTimeFormatter.ofPattern("yyyyMMdd"));

            long days = ChronoUnit.DAYS.between(startLocalDate, endLocalDate) + 1;
            if (days != 7) {
                throw new CoreException(
                        ErrorType.BAD_REQUEST,
                        "주간 랭킹은 정확히 7일 기간이어야 합니다. (입력된 기간: " + days + "일)"
                );
            }

            return new RankingViewCriteria.SearchWeeklyRanking(page, size, startLocalDate, endLocalDate);
        }
    }


    public record SearchMonthlyRankingRequest(
            @NotNull
            int page,
            @NotNull
            int size,
            @NotNull
            String startDate,
            @NotNull
            String endDate
    ){
        public RankingViewCriteria.SearchMonthlyRanking toCriteria(){
            LocalDate startLocalDate = LocalDate.parse(startDate, DateTimeFormatter.ofPattern("yyyyMMdd"));

            LocalDate endLocalDate = LocalDate.parse(endDate, DateTimeFormatter.ofPattern("yyyyMMdd"));

            long days = ChronoUnit.DAYS.between(startLocalDate, endLocalDate) + 1;
            if (days != 30) {
                throw new CoreException(
                        ErrorType.BAD_REQUEST,
                        "월간 랭킹은 정확히 30일 기간이어야 합니다. (입력된 기간: " + days + "일)"
                );
            }

            return new RankingViewCriteria.SearchMonthlyRanking(page, size, startLocalDate, endLocalDate);
        }
    }



}
