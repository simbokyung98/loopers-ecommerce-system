package com.loopers.application.product.dto;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration(proxyBeanMethods = false)
public class ProductDetailRedisConfig {

    /**
     * ProductInfo.Product 를 JSON으로 저장/조회하는 상세 전용 템플릿.
     * - Key: String
     * - Value: JSON (타입정보 포함)
     */
    @Bean("productDetailRedisTemplate")
    public RedisTemplate<String, ProductInfo.Product> productDetailRedisTemplate(
            LettuceConnectionFactory defaultRedisConnectionFactory
    ) {
        RedisTemplate<String, ProductInfo.Product> template = new RedisTemplate<>();
        template.setConnectionFactory(defaultRedisConnectionFactory);
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new GenericJackson2JsonRedisSerializer());
        template.afterPropertiesSet();
        return template;
    }
}
