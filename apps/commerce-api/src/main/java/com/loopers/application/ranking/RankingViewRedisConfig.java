package com.loopers.application.ranking;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration(proxyBeanMethods = false)
public class RankingViewRedisConfig {

    @Bean
    public RedisTemplate<String, RankingViewInfo.ProductList> rankingViewRedisTemplate(
            LettuceConnectionFactory defaultRedisConnectionFactory
    ) {
        RedisTemplate<String, RankingViewInfo.ProductList> template =
                new RedisTemplate<>();
        template.setConnectionFactory(defaultRedisConnectionFactory);
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new GenericJackson2JsonRedisSerializer());
        template.afterPropertiesSet();
        return template;
    }
}
