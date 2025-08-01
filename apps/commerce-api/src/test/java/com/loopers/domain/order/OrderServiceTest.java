package com.loopers.domain.order;

import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @InjectMocks
    private OrderService orderService;

    @Mock
    private OrderRepository orderRepository;



    @DisplayName("주문을 조회할 때,")
    @Nested
    class PlaceOrder {
        @DisplayName("주문이 없으면, NOT_FOUND 예외가 발생한다.")
        @Test
        void throwsNotFoundException_whenOrderEmpty(){

            Long orderId = 999L;

            when(orderRepository.findOrderById(orderId))
                    .thenReturn(Optional.empty());

            Throwable throwable = catchThrowable(() ->
                 orderService.getOrderDetailById(orderId)
            );

            assertThat(throwable)
                    .isInstanceOf(CoreException.class)
                    .extracting("errorType")
                    .isEqualTo(ErrorType.NOT_FOUND);
        }
    }
}
