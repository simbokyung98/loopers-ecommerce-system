package com.loopers.infrastructure.event;


import com.loopers.domain.event.EventHandlerModel;
import com.loopers.domain.event.EventHandlerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@RequiredArgsConstructor
@Repository
public class EventHandlerRepositoryImpl implements EventHandlerRepository {

    private final EventHandlerJpaRepository eventHandlerJpaRepository;

    @Override
    public void save(EventHandlerModel eventHandlerModel) {
        eventHandlerJpaRepository.save(eventHandlerModel);
    }

    @Override
    public boolean existsForUpdate(String eventId, String handlerName) {
        return eventHandlerJpaRepository.existsForUpdate(eventId, handlerName);
    }
}
