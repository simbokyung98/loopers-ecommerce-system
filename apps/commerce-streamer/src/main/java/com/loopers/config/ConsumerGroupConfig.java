package com.loopers.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;

@Configuration
public class ConsumerGroupConfig {

    private final ConsumerFactory<Object, Object> consumerFactory;

    public ConsumerGroupConfig(ConsumerFactory<Object, Object> consumerFactory) {
        this.consumerFactory = consumerFactory;
    }

    /**
     * 로깅 컨슈머 그룹
     * - 목적: 모든 이벤트를 빠짐없이 적재 (event_log 등)
     * - 특성: 실시간성이 중요하지 않고 안정성이 최우선
     * - 설정 이유:
     *   → concurrency = 1 : 단일 스레드로 순차 처리, 안정성 보장
     *   → pollTimeout = 3000 : poll() 대기 길게 두어 대량 배치 처리에 유리
     *   → max-poll-records 는 기본값 사용 (KafkaConfig에서 조정 가능)
     */
    @Bean(name = "loggingKafkaListenerContainerFactory")
    public ConcurrentKafkaListenerContainerFactory<Object, Object> loggingFactory() {
        var factory = new ConcurrentKafkaListenerContainerFactory<Object, Object>();
        factory.setConsumerFactory(consumerFactory);
        factory.setConcurrency(1);
        factory.getContainerProperties().setPollTimeout(3000);
        return factory;
    }

    /**
     * 집계 컨슈머 그룹
     * - 목적: 좋아요/판매량 등 지표를 실시간 집계
     * - 특성: 빠른 처리 + 병렬성이 중요
     * - 설정 이유:
     *   → concurrency = 3 : 파티션 수(3개 가정)에 맞춰 병렬 처리
     *   → pollTimeout = 1000 : 지연 최소화를 위해 1초 단위로 폴링
     *   → batch 크기는 너무 크지 않게 (KafkaConfig에서 max-poll-records=200 등 설정)
     */

    // ============================
    // [왜 기본설정(BATCH_LISTENER_DEFAULT) 대신 별도 Factory를 쓰는가?]
    //
    // 기본 BATCH_LISTENER_DEFAULT 설정:
    //   - max.poll.records = 3000
    //   - fetch.min.bytes  = 1MB
    //   - fetch.max.wait.ms = 5초
    //   → 대량 메시지를 모아서 배치로 처리하는 데 유리 (로그 적재, 데이터 아카이빙)
    //   → 하지만 집계에는 단점:
    //      * poll 주기가 길어져 실시간성이 떨어짐
    //      * 한 번에 너무 많은 이벤트를 모으다 보니 지연(latency) 발생
    //
    // Metrics 전용 설정(집계 컨슈머):
    //   - pollTimeout = 1000ms → 1초 단위로 짧게 poll
    //   - concurrency = 3 → 파티션 수에 맞춰 병렬 처리
    //   - batchListener = true → 이벤트를 모아 한 번에 DB 반영
    //   - ackMode = MANUAL → 수동 커밋으로 멱등성 제어 가능
    //
    // 결론:
    //   * 기본 배치 리스너는 "대량/느긋한 처리" 용도
    //   * metricsFactory 는 "실시간 집계/빠른 반영" 용도
    // ============================

    @Bean(name = "metricsKafkaListenerContainerFactory")
    public ConcurrentKafkaListenerContainerFactory<Object, Object> metricsFactory() {
        var factory = new ConcurrentKafkaListenerContainerFactory<Object, Object>();
        factory.setConsumerFactory(consumerFactory);
        factory.setConcurrency(3);
        factory.setBatchListener(true); // ✅ 배치 모드
        factory.getContainerProperties().setPollTimeout(1000);
        return factory;
    }

    /**
     * 캐시 컨슈머 그룹
     * - 목적: 좋아요순 상품 캐시 무효화 → UX에 직접 반영
     * - 특성: 초저지연이 중요, 처리 로직이 가벼움
     * - 설정 이유:
     *   → concurrency = 2 : 캐시 무효화는 병목이 적지만, 가볍게 2개 스레드로 분산
     *   → pollTimeout = 500 : 가능한 빠른 응답을 위해 짧은 폴링 간격
     *   → max-poll-records 는 소량(기본값 혹은 50 수준)으로 유지
     */
    @Bean(name = "cacheKafkaListenerContainerFactory")
    public ConcurrentKafkaListenerContainerFactory<Object, Object> cacheFactory() {
        var factory = new ConcurrentKafkaListenerContainerFactory<Object, Object>();
        factory.setConsumerFactory(consumerFactory);
        factory.setConcurrency(2);
        factory.getContainerProperties().setPollTimeout(500);
        return factory;
    }
}
