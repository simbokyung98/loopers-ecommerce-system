package com.loopers.confg.kafka;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

public record KafkaMessage<T>(
        String eventId,
        String publishedAt,
        T payload
) {
    // payload만 넘기면 id/at 자동 채워서 래핑
    public static <T> KafkaMessage<T> from(T payload) {
        return new KafkaMessage<>(
                UUID.randomUUID().toString(),
                OffsetDateTime.now(ZoneOffset.UTC).toString(),
                payload
        );
    }

}
