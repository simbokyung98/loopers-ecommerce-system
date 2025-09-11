package com.loopers.support;

import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
public class RankingDailyKeyResolver {
    public String dailyKey(LocalDate date) {
        return "ranking:product:total:daily:" + date;
    }
}
