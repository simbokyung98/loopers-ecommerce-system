package com.loopers.domain.event;


import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class EventHandlerService {

    private final EventHandlerRepository repository;

    @Transactional
    public boolean tryConsume(String eventId, String handlerName) {
        // 존재 여부 확인 (행 잠금)
        boolean exists = repository.existsForUpdate(eventId, handlerName);
        if (exists) {
            return false;
        }
        repository.save(new EventHandlerModel(eventId, handlerName));
        return true;
    }
}
