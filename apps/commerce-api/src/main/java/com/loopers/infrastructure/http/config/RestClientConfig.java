package com.loopers.infrastructure.http.config;


import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestClient;

@Configuration
@ConfigurationProperties(prefix = "external")
@Getter @Setter
public class RestClientConfig {

    private ClientProperties pg;

    @Getter
    @Setter
    public static class ClientProperties {

        private String baseUrl;

    }

    @Bean
    public RestClient pgClient(){

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        return RestClient.builder()
                .baseUrl(pg.getBaseUrl())
                .defaultHeaders(httpHeaders -> httpHeaders.addAll(headers))
                .build();
    }

}
