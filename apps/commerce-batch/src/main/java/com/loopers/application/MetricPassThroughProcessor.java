// src/main/java/com/loopers/config/MetricPassThroughProcessor.java
package com.loopers.application;

import com.loopers.application.dto.AggregateRow;
import com.loopers.application.dto.ScoredAggregate;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

@Component
public class MetricPassThroughProcessor implements ItemProcessor<AggregateRow, ScoredAggregate> {

    private static final int W_VIEW  = 1;
    private static final int W_LIKE  = 2;
    private static final int W_ORDER = 5;

    @Override
    public ScoredAggregate process(AggregateRow r) {
        long score = r.viewSum()*W_VIEW + r.likeSum()*W_LIKE + r.orderSum()*W_ORDER;
        return new ScoredAggregate(r.productId(), r.viewSum(), r.likeSum(), r.orderSum(), score);
    }
}
