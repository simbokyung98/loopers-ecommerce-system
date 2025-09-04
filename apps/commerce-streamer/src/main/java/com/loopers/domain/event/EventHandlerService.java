package com.loopers.domain.event;


import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class EventHandlerService {

    private final EventHandlerRepository repository;

    @Transactional
    public boolean tryConsume(String eventId, String handlerName) {
        try {
            repository.save(new EventHandlerModel(eventId, handlerName));
            return true;
        } catch (DataIntegrityViolationException e) {
            return false; // 중복 키 발생 시 스킵
        }
    }
}
