package com.loopers.cache.ranking;

import com.loopers.application.ranking.RankingType;
import org.springframework.stereotype.Component;

@Component
public class RankingViewKeyBuilder {

    private static final String PREFIX = "rank:list";

    public String build(RankingType sort, int page, int size) {
        String sortKey = sort.getValue();
        return String.format("%s:%s:p%d:s%d",PREFIX, sortKey, page, size);
    }

}
