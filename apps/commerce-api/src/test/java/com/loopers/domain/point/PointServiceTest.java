package com.loopers.domain.point;

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

import static org.assertj.core.api.Assertions.assertThatException;
import static org.assertj.core.api.InstanceOfAssertFactories.type;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PointServiceTest {

    @InjectMocks
    private PointService pointService;

    @Mock
    private PointRepository pointRepository;

    @DisplayName("포인트를 충전을 할 때, ")
    @Nested
    class Charge {
        @DisplayName("해당 유저의 포인트가 존재하지 않으면, BAD_REQUEST 예외가 발생하며 실패한다.")
        @Test
        void throwsException_whenUserDoesNotExist(){

            Long userId = 1L;
            Long point = 1000L;

            when(pointRepository.findByUserId(userId))
                            .thenReturn(Optional.empty());

            assertThatException()
                    .isThrownBy(() -> pointService.charge(userId, point))
                    .isInstanceOf(CoreException.class)
                    .extracting("errorType", type(ErrorType.class))
                    .isEqualTo(ErrorType.BAD_REQUEST);
        }

    }

    @DisplayName("포인트를 사용할 때, ")
    @Nested
    class Spend {
        @DisplayName("해당 유저의 포인트가 존재하지 않으면, BAD_REQUEST 예외가 발생하며 실패한다.")
        @Test
        void throwsException_whenUserDoesNotExist(){

            Long userId = 1L;
            Long point = 1000L;

            when(pointRepository.findByUserIdForUpdate(userId))
                    .thenReturn(Optional.empty());

            assertThatException()
                    .isThrownBy(() -> pointService.spend(userId, point))
                    .isInstanceOf(CoreException.class)
                    .extracting("errorType", type(ErrorType.class))
                    .isEqualTo(ErrorType.BAD_REQUEST);
        }
    }
}
