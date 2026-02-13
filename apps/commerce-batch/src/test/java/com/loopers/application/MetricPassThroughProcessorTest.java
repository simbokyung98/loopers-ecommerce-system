package com.loopers.application;


import com.loopers.application.dto.AggregateRow;
import com.loopers.application.dto.ScoredAggregate;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class MetricPassThroughProcessorTest {

    private final MetricPassThroughProcessor sut = new MetricPassThroughProcessor();

    @Test
    void score_is_calculated_with_weights_1_2_5() throws Exception {
        // given

        AggregateRow row = new AggregateRow(10L, 100, 20, 3);

        // when
        ScoredAggregate out = sut.process(row);

        // then
        // score = 100*1 + 20*2 + 3*5 = 100 + 40 + 15 = 155
        assertNotNull(out);
        assertEquals(10L, out.productId());
        assertEquals(100, out.viewSum());
        assertEquals(20, out.likeSum());
        assertEquals(3, out.orderSum());
        assertEquals(155, out.score());
    }
}
