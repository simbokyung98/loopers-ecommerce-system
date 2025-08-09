package com.loopers.application.common;


import org.springframework.data.domain.Page;

import java.util.List;

public class PageInfo {
    public record PageMeta(
            int page,
            int size,
            long totalElements,
            int totalPages
    ) {}

    public record PageEnvelope<T>(
            List<T> content,
            PageMeta meta
    ) {
        public static <T> PageEnvelope<T> from(Page<T> page) {
            return new PageEnvelope<>(
                    page.getContent(),
                    new PageMeta(page.getNumber(), page.getSize(), page.getTotalElements(), page.getTotalPages())
            );
        }
        public static <T> PageEnvelope<T> of(List<T> items, PageMeta meta) {
            return new PageEnvelope<>(items, meta);
        }
    }
}
