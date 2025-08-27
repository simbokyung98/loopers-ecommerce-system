package com.loopers.domain.payment;

import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PaymentModelTest {

    @Test
    @DisplayName("orderId가 null이면 BAD_REQUEST 예외")
    void throwsBadRequest_whenOrderIdNull() {
        assertThatThrownBy(() -> new PaymentModel(null, 1L, PaymentType.POINT, 100L))
                .isInstanceOf(CoreException.class)
                .satisfies(e -> assertThat(((CoreException) e).getErrorType()).isEqualTo(ErrorType.BAD_REQUEST));
    }

    @Test
    @DisplayName("userId가 null이면 BAD_REQUEST 예외")
    void throwsBadRequest_whenUserIdNull() {
        assertThatThrownBy(() -> new PaymentModel(1L, null, PaymentType.POINT, 100L))
                .isInstanceOf(CoreException.class)
                .satisfies(e -> assertThat(((CoreException) e).getErrorType()).isEqualTo(ErrorType.BAD_REQUEST));
    }

    @Test
    @DisplayName("type이 null이면 BAD_REQUEST 예외")
    void throwsBadRequest_whenTypeNull() {
        assertThatThrownBy(() -> new PaymentModel(1L, 1L, null, 100L))
                .isInstanceOf(CoreException.class)
                .satisfies(e -> assertThat(((CoreException) e).getErrorType()).isEqualTo(ErrorType.BAD_REQUEST));
    }

    @Test
    @DisplayName("amount가 null/0/음수면 BAD_REQUEST 예외")
    void throwsBadRequest_whenAmountNullOrNonPositive() {
        assertThatThrownBy(() -> new PaymentModel(1L, 1L, PaymentType.POINT, null))
                .isInstanceOf(CoreException.class)
                .satisfies(e -> assertThat(((CoreException) e).getErrorType()).isEqualTo(ErrorType.BAD_REQUEST));
        assertThatThrownBy(() -> new PaymentModel(1L, 1L, PaymentType.POINT, 0L))
                .isInstanceOf(CoreException.class)
                .satisfies(e -> assertThat(((CoreException) e).getErrorType()).isEqualTo(ErrorType.BAD_REQUEST));
        assertThatThrownBy(() -> new PaymentModel(1L, 1L, PaymentType.POINT, -1L))
                .isInstanceOf(CoreException.class)
                .satisfies(e -> assertThat(((CoreException) e).getErrorType()).isEqualTo(ErrorType.BAD_REQUEST));
    }


    @Test
    @DisplayName("point(): orderId가 null이면 BAD_REQUEST 예외")
    void point_throwsBadRequest_whenOrderIdNull() {
        assertThatThrownBy(() -> PaymentModel.point(null, 1L, 100L, PaymentStatus.SUCCEEDED))
                .isInstanceOf(CoreException.class)
                .satisfies(e -> assertThat(((CoreException) e).getErrorType()).isEqualTo(ErrorType.BAD_REQUEST));
    }

    @Test
    @DisplayName("point(): userId가 null이면 BAD_REQUEST 예외")
    void point_throwsBadRequest_whenUserIdNull() {
        assertThatThrownBy(() -> PaymentModel.point(1L, null, 100L, PaymentStatus.SUCCEEDED))
                .isInstanceOf(CoreException.class)
                .satisfies(e -> assertThat(((CoreException) e).getErrorType()).isEqualTo(ErrorType.BAD_REQUEST));
    }

    @Test
    @DisplayName("point(): amount가 null/0/음수면 BAD_REQUEST 예외")
    void point_throwsBadRequest_whenAmountNullOrNonPositive() {
        assertThatThrownBy(() -> PaymentModel.point(1L, 1L, null, PaymentStatus.SUCCEEDED))
                .isInstanceOf(CoreException.class)
                .satisfies(e -> assertThat(((CoreException) e).getErrorType()).isEqualTo(ErrorType.BAD_REQUEST));
        assertThatThrownBy(() -> PaymentModel.point(1L, 1L, 0L, PaymentStatus.SUCCEEDED))
                .isInstanceOf(CoreException.class)
                .satisfies(e -> assertThat(((CoreException) e).getErrorType()).isEqualTo(ErrorType.BAD_REQUEST));
    }

    @Test
    @DisplayName("point(): status가 null이면 BAD_REQUEST 예외")
    void point_throwsBadRequest_whenStatusNull() {
        assertThatThrownBy(() -> PaymentModel.point(1L, 1L, 100L, null))
                .isInstanceOf(CoreException.class)
                .satisfies(e -> assertThat(((CoreException) e).getErrorType()).isEqualTo(ErrorType.BAD_REQUEST));
    }


    @Test
    @DisplayName("cardPending(): orderId가 null이면 BAD_REQUEST 예외")
    void cardPending_throwsBadRequest_whenOrderIdNull() {
        assertThatThrownBy(() -> PaymentModel.cardPending(null, 1L, 100L, "tx-1"))
                .isInstanceOf(CoreException.class)
                .satisfies(e -> assertThat(((CoreException) e).getErrorType()).isEqualTo(ErrorType.BAD_REQUEST));
    }

    @Test
    @DisplayName("cardPending(): userId가 null이면 BAD_REQUEST 예외")
    void cardPending_throwsBadRequest_whenUserIdNull() {
        assertThatThrownBy(() -> PaymentModel.cardPending(1L, null, 100L, "tx-1"))
                .isInstanceOf(CoreException.class)
                .satisfies(e -> assertThat(((CoreException) e).getErrorType()).isEqualTo(ErrorType.BAD_REQUEST));
    }

    @Test
    @DisplayName("cardPending(): amount가 null/0/음수면 BAD_REQUEST 예외")
    void cardPending_throwsBadRequest_whenAmountNullOrNonPositive() {
        assertThatThrownBy(() -> PaymentModel.cardPending(1L, 1L, null, "tx-1"))
                .isInstanceOf(CoreException.class)
                .satisfies(e -> assertThat(((CoreException) e).getErrorType()).isEqualTo(ErrorType.BAD_REQUEST));
        assertThatThrownBy(() -> PaymentModel.cardPending(1L, 1L, 0L, "tx-1"))
                .isInstanceOf(CoreException.class)
                .satisfies(e -> assertThat(((CoreException) e).getErrorType()).isEqualTo(ErrorType.BAD_REQUEST));
    }

    @Test
    @DisplayName("cardPending(): pgTxId가 null/blank면 BAD_REQUEST 예외")
    void cardPending_throwsBadRequest_whenTxKeyNullOrBlank() {
        assertThatThrownBy(() -> PaymentModel.cardPending(1L, 1L, 100L, null))
                .isInstanceOf(CoreException.class)
                .satisfies(e -> assertThat(((CoreException) e).getErrorType()).isEqualTo(ErrorType.BAD_REQUEST));
        assertThatThrownBy(() -> PaymentModel.cardPending(1L, 1L, 100L, " "))
                .isInstanceOf(CoreException.class)
                .satisfies(e -> assertThat(((CoreException) e).getErrorType()).isEqualTo(ErrorType.BAD_REQUEST));
    }
}
