package com.loopers.application.ranking;

import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum RankingType {
    일일랭킹("DAILY_RANKING");

    private final String value;

    public static RankingType fromValue(String value) {
        for (RankingType type : RankingType.values()) {
            if (type.value.equalsIgnoreCase(value)) {
                return type;
            }
        }
        throw new CoreException(ErrorType.BAD_REQUEST, "잘못된 랭킹 타입입니다.");
    }
}
