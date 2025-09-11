package com.loopers.application.ranking;

import java.time.LocalDate;

public record RankingCarryOverEvent(LocalDate date, double factor) {}
