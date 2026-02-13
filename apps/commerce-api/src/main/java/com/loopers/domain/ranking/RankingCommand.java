package com.loopers.domain.ranking;

import java.time.LocalDate;

public class RankingCommand {

    public record SearchWeeklyRanking(
            int page,
            int size,
            LocalDate startDate,
            LocalDate endDate
    ){}

    public record SearchMonthlyRanking(
            int page,
            int size,
            LocalDate startDate,
            LocalDate endDate
    ){}
}
