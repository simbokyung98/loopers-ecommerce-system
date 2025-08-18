package com.loopers.cache;

import org.springframework.stereotype.Component;
import com.loopers.interfaces.api.product.OrderType;

@Component
public class ProductListKeyBuilder {
    private static final String PREFIX = "prod:list";

    /** 키 규칙: prod:list:{sort}:{brand|all}:p{page}:s{size} */
    public String build(OrderType sort, Long brandId, int page, int size) {
        String sortKey = sort.name(); // enum에 value 필드가 있으면 getValue()로 "일관되게" 사용
        String brandPart = (brandId == null) ? "all" : String.valueOf(brandId);
        return String.format("%s:%s:%s:p%d:s%d", PREFIX, sortKey, brandPart, page, size);
    }
}
