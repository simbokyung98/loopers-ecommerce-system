package com.loopers.infrastructure.event;


import com.loopers.domain.event.EventHandlerId;
import com.loopers.domain.event.EventHandlerModel;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EventHandlerJpaRepository extends JpaRepository<EventHandlerModel, EventHandlerId> {
}
