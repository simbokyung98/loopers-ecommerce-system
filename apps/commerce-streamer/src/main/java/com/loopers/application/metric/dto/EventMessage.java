package com.loopers.application.metric.dto;

public record EventMessage(
        String eventId,
        String publishedAt
) {
}
