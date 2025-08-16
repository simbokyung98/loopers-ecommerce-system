package com.loopers.cache;

import java.time.Duration;

public record CachePolicy(
        Duration ttl,
        int maxPageInclusive,
        Duration refreshAhead,
        int prewarmPages
) {
    public static CachePolicy of(Duration ttl, int maxPage) {
        return new CachePolicy(ttl, maxPage, Duration.ZERO, 0);
    }
    public static CachePolicy of(Duration ttl, int maxPage, Duration refreshAhead, int prewarmPages) {
        return new CachePolicy(ttl, maxPage, refreshAhead, prewarmPages);
    }

    public boolean refreshAheadEnabled() {
        return refreshAhead != null && !refreshAhead.isZero() && !refreshAhead.isNegative();
    }

    public boolean shouldRefreshAhead(Long remainSeconds) {
        if (!refreshAheadEnabled() || remainSeconds == null || remainSeconds <= 0) return false;
        return remainSeconds <= refreshAhead.getSeconds();
    }
}
