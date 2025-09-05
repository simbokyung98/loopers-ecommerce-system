package com.loopers.domain.event;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@IdClass(EventHandlerId.class)
@Table(name = "event_handler")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class EventHandlerModel {

    @Id
    @Column(name = "event_id", length = 36, nullable = false)
    private String eventId;

    @Id
    @Column(name = "handler_name", length = 100, nullable = false)
    private String handlerName;

    @Column(name = "consumed_at", nullable = false, updatable = false)
    private LocalDateTime consumedAt = LocalDateTime.now();


    public EventHandlerModel(String eventId, String handlerName) {
        this.eventId = eventId;
        this.handlerName = handlerName;
    }




}
