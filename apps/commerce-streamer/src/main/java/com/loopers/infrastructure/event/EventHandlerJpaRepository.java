package com.loopers.infrastructure.event;


import com.loopers.domain.event.EventHandlerId;
import com.loopers.domain.event.EventHandlerModel;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface EventHandlerJpaRepository extends JpaRepository<EventHandlerModel, EventHandlerId> {


    @Query("select case when count(e) > 0 then true else false end " +
            "from EventHandlerModel e " +
            "where e.eventId = :eventId and e.handlerName = :handlerName")
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    boolean existsForUpdate(@Param("eventId") String eventId,
                            @Param("handlerName") String handlerName);
}
