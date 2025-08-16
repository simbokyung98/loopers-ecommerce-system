package com.loopers.application.product.dto;

import com.loopers.application.common.PageInfo;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration(proxyBeanMethods = false)
public class ProductPageRedisConfig {

    /**
     * PageEnvelope<ProductInfo.Product> 를 JSON으로 저장/조회하는 템플릿.
     * - Key: String (StringRedisSerializer)
     * - Value: JSON (GenericJackson2JsonRedisSerializer)  ← 타입정보 포함
     *
     */
    @Bean
    public RedisTemplate<String, PageInfo.PageEnvelope<ProductInfo.Product>> productPageRedisTemplate(
            LettuceConnectionFactory defaultRedisConnectionFactory
    ) {
        RedisTemplate<String, PageInfo.PageEnvelope<ProductInfo.Product>> template =
                new RedisTemplate<>();
        template.setConnectionFactory(defaultRedisConnectionFactory);
        template.setKeySerializer(new StringRedisSerializer());
        // 타입정보를 JSON에 포함하는 직렬화기 → 제네릭 T도 안전하게 역직렬화됨
        template.setValueSerializer(new GenericJackson2JsonRedisSerializer());
        template.afterPropertiesSet();
        return template;
    }
}
