package com.loopers.config;


import org.apache.kafka.common.TopicPartition;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.support.converter.BatchMessagingMessageConverter;
import org.springframework.kafka.support.converter.ByteArrayJsonMessageConverter;
import org.springframework.kafka.support.converter.ConversionException;
import org.springframework.kafka.support.serializer.DeserializationException;
import org.springframework.util.backoff.FixedBackOff;

/**
 * 왜 기본(Spring Boot 자동 구성) 대신 커스텀 컨테이너 팩토리를 쓰나?
 *
 * 1) 컨슈머의 목적/패턴이 서로 다름
 *    - metrics(집계): "배치로 모아서 DB에 반영" + "실시간성" → 배치 리스너 + 짧은 poll + 수동 커밋 필요
 *    - cache(캐시): "초저지연 단건 처리" → 단건 리스너 + 더 짧은 poll + 수동 커밋 필요
 *
 * 2) YAML의 공통 설정은 연결/직렬화/리트라이 등 '클라이언트 레벨'을 담당.
 *    여긴 '리스너 컨테이너 동작(배치/ACK/에러처리/동시성)'을 목적별로 다르게 세팅.
 *
 * 3) 이전 오류 재발 방지
 *    - "No Acknowledgment available..." 문제는 컨테이너 AckMode가 MANUAL이 아닐 때 발생.
 *      → 각 팩토리에서 명시적으로 MANUAL 설정(배치/단건 모두)로 고정.
 */
@Configuration
public class ConsumerGroupConfig {

    private final ConsumerFactory<Object, Object> consumerFactory;

    public ConsumerGroupConfig(ConsumerFactory<Object, Object> consumerFactory) {
        this.consumerFactory = consumerFactory; // Boot가 application.yml 기반으로 만들어 준 공용 ConsumerFactory 사용
    }


    /**
     * 공통 에러 핸들러
     * - 일시 오류는 짧게 재시도(FixedBackOff), 영구 오류(역직렬화/변환/명백한 입력 오류)는 즉시 DLT.
     * - DLT로 보낸 레코드는 커밋(CommitRecovered=true)해서 무한 재처리 방지.
     * - DLT 토픽명: <원본토픽>.DLT (파티션 동일)
     */
    private DefaultErrorHandler commonErrorHandler(KafkaTemplate<Object, Object> template) {
        DeadLetterPublishingRecoverer recoverer = new DeadLetterPublishingRecoverer(
                template,
                (rec, ex) -> new TopicPartition(rec.topic() + ".DLT", rec.partition())
        );

        DefaultErrorHandler handler = new DefaultErrorHandler(
                recoverer,
                // 1초 간격, 최대 3회 재시도 → 짧은 네트워크/일시 DB 장애 복구용
                new FixedBackOff(1000L, 3)
        );

        // 데이터 자체 문제는 재시도 무의미 → 즉시 DLT
        handler.addNotRetryableExceptions(
                DeserializationException.class,   // 역직렬화 실패
                ConversionException.class,        // 메시지 변환 실패
                IllegalArgumentException.class    // 명백한 유효성/필드 오류 등
        );

        handler.setCommitRecovered(true);    // DLT 전송 후 오프셋 커밋(루프 방지)
        return handler;
    }

    // ----------------------------------------------------------------------
    // [metrics] 집계 컨슈머: "실시간 배치 집계"에 최적화
    //  - 배치 리스너로 DB I/O 횟수 절감
    //  - 짧은 poll 간격으로 지연 최소화
    //  - 수동 커밋으로 멱등성/정확한 처리 보장
    // ----------------------------------------------------------------------
    @Bean(name = "metricsKafkaListenerContainerFactory")
    public ConcurrentKafkaListenerContainerFactory<Object, Object> metricsKafkaListenerContainerFactory(
            ByteArrayJsonMessageConverter jsonConverter,
            KafkaTemplate<Object, Object> kafkaTemplate
    ) {
        var factory = new ConcurrentKafkaListenerContainerFactory<Object, Object>();
        factory.setConsumerFactory(consumerFactory);

        // 파티션 수에 맞춰 병렬 처리(예: 3 파티션 가정)
        // - 이유: 병렬로 배치를 처리해 처리량/지연을 동시에 잡는다.
        factory.setConcurrency(3);

        // 배치 리스너로 동작
        // - 이유: 여러 레코드를 모아 한 번에 DB에 반영 → 트랜잭션/커넥션 비용 절감
        factory.setBatchListener(true);

        // byte[] → 도메인 객체 리스트 변환 (배치 전용 컨버터)
        // - 이유: value-deserializer가 ByteArray인 상황에서 @KafkaListener 파라미터 타입으로 변환하려면 필수
        factory.setBatchMessageConverter(new BatchMessagingMessageConverter(jsonConverter));

        // 컨테이너 동작 세부
        var props = factory.getContainerProperties();
        props.setPollTimeout(1000);                           // 1초마다 poll → 실시간성 유지(대기 과도 방지)
        props.setAckMode(ContainerProperties.AckMode.MANUAL); // 수동 커밋 → 처리 성공 후에만 커밋(멱등성/재처리 제어)
        props.setSyncCommits(true);                           // 커밋 응답 대기 → 중복 소비/유실 리스크 감소
        props.setObservationEnabled(true);                    // Micrometer 관측(메트릭/트레이싱)
        props.setDeliveryAttemptHeader(true);                 // 재시도 횟수 헤더 노출 → 운영 관측성 향상

        // 공통 에러 처리기(재시도 + DLT)
        factory.setCommonErrorHandler(commonErrorHandler(kafkaTemplate));

        return factory;
    }

    // ----------------------------------------------------------------------
    // [cache] 캐시 컨슈머: "초저지연 단건 처리"에 최적화
    //  - 단건 리스너로 즉시 처리
    //  - 더 짧은 poll, 수동 커밋
    // ----------------------------------------------------------------------
    @Bean(name = "cacheKafkaListenerContainerFactory")
    public ConcurrentKafkaListenerContainerFactory<Object, Object> cacheKafkaListenerContainerFactory(
            ByteArrayJsonMessageConverter jsonConverter,
            KafkaTemplate<Object, Object> kafkaTemplate
    ) {
        var factory = new ConcurrentKafkaListenerContainerFactory<Object, Object>();
        factory.setConsumerFactory(consumerFactory);

        // 캐시 무효화 로직은 가벼움 → 과도한 스레드 불필요(여유 있게 2)
        factory.setConcurrency(2);

        // 단건 메시지 변환기 사용(byte[] → 도메인)
        // - 이유: 단건 리스너에서 @Payload 도메인 타입 매핑
        factory.setRecordMessageConverter(jsonConverter);

        var props = factory.getContainerProperties();
        props.setPollTimeout(500);                            // 더 짧게 poll → UI 반영 지연 최소화
        props.setAckMode(ContainerProperties.AckMode.MANUAL); // 수동 커밋 → 처리 성공 시점에만 오프셋 커밋
        props.setSyncCommits(true);                           // 커밋 동기화로 안전성 확보
        props.setObservationEnabled(true);                    // 관측 가능성 확보
        props.setDeliveryAttemptHeader(true);                 // 재시도 횟수 헤더 노출

        // 공통 에러 처리기(재시도 + DLT)
        factory.setCommonErrorHandler(commonErrorHandler(kafkaTemplate));

        return factory;
    }
}
