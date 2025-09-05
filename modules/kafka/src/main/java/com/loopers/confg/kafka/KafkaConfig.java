package com.loopers.confg.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.*;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.support.converter.BatchMessagingMessageConverter;
import org.springframework.kafka.support.converter.ByteArrayJsonMessageConverter;

import java.util.HashMap;
import java.util.Map;

@EnableKafka // Kafka 리스너(@KafkaListener) 사용 가능하게 함
@Configuration // 스프링 설정 클래스
@EnableConfigurationProperties(KafkaProperties.class) // application.yml의 spring.kafka.* 값 주입
public class KafkaConfig {
    public static final String BATCH_LISTENER = "BATCH_LISTENER_DEFAULT"; // 배치 리스너 팩토리 이름

    // Consumer 옵션
    public static final int MAX_POLLING_SIZE = 3000; // 한 번에 최대 3000개 메시지 가져오기
    public static final int FETCH_MIN_BYTES = (1024 * 1024); // 최소 1MB 이상 모아야 전송
    public static final int FETCH_MAX_WAIT_MS = 5 * 1000; // 최대 5초 기다린 뒤 전송
    public static final int SESSION_TIMEOUT_MS = 60 * 1000; // 1분 동안 응답 없으면 세션 끊김
    public static final int HEARTBEAT_INTERVAL_MS = 20 * 1000; // 하트비트 주기 (20초)
    public static final int MAX_POLL_INTERVAL_MS = 2 * 60 * 1000; // 메시지 처리 후 2분 안에 poll() 호출해야 함

    @Bean
    public ProducerFactory<Object, Object> producerFactory(KafkaProperties kafkaProperties) {
        // Kafka Producer를 만드는 공장 (application.yml 설정 사용)
        Map<String, Object> props = new HashMap<>(kafkaProperties.buildProducerProperties());
        return new DefaultKafkaProducerFactory<>(props);
    }

    @Bean
    public ConsumerFactory<Object, Object> consumerFactory(KafkaProperties kafkaProperties) {
        // Kafka Consumer를 만드는 공장 (application.yml 설정 사용)
        Map<String, Object> props = new HashMap<>(kafkaProperties.buildConsumerProperties());
        return new DefaultKafkaConsumerFactory<>(props);
    }

    @Bean
    public KafkaTemplate<Object, Object> kafkaTemplate(ProducerFactory<Object, Object> producerFactory) {
        // 메시지 전송할 때 사용하는 핵심 객체
        return new KafkaTemplate<>(producerFactory);
    }

    @Bean
    public ByteArrayJsonMessageConverter jsonMessageConverter(ObjectMapper objectMapper) {
        // byte[] ↔ JSON 변환기
        return new ByteArrayJsonMessageConverter(objectMapper);
    }

    @Bean(name = BATCH_LISTENER)
    public ConcurrentKafkaListenerContainerFactory<Object, Object> defaultBatchListenerContainerFactory(
            KafkaProperties kafkaProperties,
            ByteArrayJsonMessageConverter converter
    ) {
        // Consumer 설정값 세팅
        Map<String, Object> consumerConfig = new HashMap<>(kafkaProperties.buildConsumerProperties());
        consumerConfig.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, MAX_POLLING_SIZE);
        consumerConfig.put(ConsumerConfig.FETCH_MIN_BYTES_CONFIG, FETCH_MIN_BYTES);
        consumerConfig.put(ConsumerConfig.FETCH_MAX_WAIT_MS_CONFIG, FETCH_MAX_WAIT_MS);
        consumerConfig.put(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, SESSION_TIMEOUT_MS);
        consumerConfig.put(ConsumerConfig.HEARTBEAT_INTERVAL_MS_CONFIG, HEARTBEAT_INTERVAL_MS);
        consumerConfig.put(ConsumerConfig.MAX_POLL_INTERVAL_MS_CONFIG, MAX_POLL_INTERVAL_MS);

        // 배치 리스너 컨테이너 생성
        ConcurrentKafkaListenerContainerFactory<Object, Object> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(new DefaultKafkaConsumerFactory<>(consumerConfig));
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL); // 수동 커밋
        factory.setBatchMessageConverter(new BatchMessagingMessageConverter(converter)); // JSON 변환
        factory.setConcurrency(3); // 3개 스레드로 병렬 처리
        factory.setBatchListener(true); // 메시지를 배치 단위로 처리
        return factory;
    }
}
