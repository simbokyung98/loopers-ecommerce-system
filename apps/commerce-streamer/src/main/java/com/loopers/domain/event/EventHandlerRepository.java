package com.loopers.domain.event;

public interface EventHandlerRepository {

    void save(EventHandlerModel eventHandlerModel);

    boolean existsForUpdate(String eventId, String handlerName);
}
