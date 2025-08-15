package com.loopers.cache;

import org.springframework.stereotype.Component;

@Component
public class ProductDetailKeyBuilder {
    public String key(Long productId) {
        return "prod:detail:" + productId;
    }
}
