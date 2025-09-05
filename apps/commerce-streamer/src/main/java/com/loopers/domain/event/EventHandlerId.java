package com.loopers.domain.event;

import java.io.Serializable;
import java.util.Objects;

public class EventHandlerId implements Serializable {
    private String eventId;
    private String handlerName;

    public EventHandlerId() {
    }

    public EventHandlerId(String eventId, String handlerName) {
        this.eventId = eventId;
        this.handlerName = handlerName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof EventHandlerId)) return false;
        EventHandlerId that = (EventHandlerId) o;
        return Objects.equals(eventId, that.eventId) &&
                Objects.equals(handlerName, that.handlerName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(eventId, handlerName);
    }

}
