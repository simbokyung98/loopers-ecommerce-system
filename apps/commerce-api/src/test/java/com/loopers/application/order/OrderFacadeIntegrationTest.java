package com.loopers.application.order;


import com.loopers.application.order.dto.OrderCriteria;
import com.loopers.application.order.dto.OrderInfo;
import com.loopers.domain.coupon.CouponRepository;
import com.loopers.domain.coupon.IssuedCouponRepository;
import com.loopers.domain.coupon.model.CouponModel;
import com.loopers.domain.coupon.model.FixedCouponModel;
import com.loopers.domain.coupon.model.IssuedCouponModel;
import com.loopers.domain.order.event.OrderCreatedEvent;
import com.loopers.domain.payment.PaymentType;
import com.loopers.domain.payment.event.PaymentFailedEvent;
import com.loopers.domain.product.ProductModel;
import com.loopers.domain.product.ProductRepository;
import com.loopers.domain.product.ProductStatus;
import com.loopers.domain.user.UserModel;
import com.loopers.domain.user.UserRepository;
import com.loopers.interfaces.api.User.Gender;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import com.loopers.utils.DatabaseCleanUp;
import com.loopers.utils.RedisCleanUp;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.event.ApplicationEvents;
import org.springframework.test.context.event.RecordApplicationEvents;

import java.time.ZonedDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@RecordApplicationEvents
public class OrderFacadeIntegrationTest {

    @Autowired
    private OrderFacade orderFacade;
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CouponRepository couponRepository;

    @Autowired
    private IssuedCouponRepository issuedCouponRepository;

    @Autowired
    private RedisCleanUp redisCleanUp;

    @Autowired
    private DatabaseCleanUp databaseCleanUp;

    @MockitoBean
    private KafkaTemplate<Object, Object> kafkaTemplate;
    @AfterEach
    void tearDown() {
        databaseCleanUp.truncateAllTables();
        redisCleanUp.truncateAll();
    }


    @DisplayName("주문할 떄,")
    @Nested
    class Order {

        @Test
        @DisplayName("쿠폰 없이 주문: 총액 그대로 이벤트 발행 + 재고 차감")
        void order_without_coupon(ApplicationEvents events) {
            // given
            UserModel user = userRepository.save(new UserModel(
                    "userA", Gender.MALE.getCode(), "1990-01-01", "a@test.com"));

            Long BRAND_ID = 1L;
            ProductModel p1 = productRepository.saveProduct(new ProductModel("A", 5L, 5_000L, ProductStatus.SELL, BRAND_ID));
            ProductModel p2 = productRepository.saveProduct(new ProductModel("B", 10L, 1_000L, ProductStatus.SELL, BRAND_ID));

            List<OrderCriteria.ProductQuantity> items = List.of(
                    new OrderCriteria.ProductQuantity(p1.getId(), 2L), // 10_000
                    new OrderCriteria.ProductQuantity(p2.getId(), 4L)  // 4_000
            );
            long expectedTotal = 14_000L;

            OrderCriteria.Order req = new OrderCriteria.Order(
                    user.getId(), null, "주소", "01011112222", "홍길동",
                    PaymentType.CARD, "SAMSUNG","1111-2222-3333-4444", items
            );

            // when
            OrderInfo.OrderResponse res = orderFacade.order(req);

            // then: 최종금액, 재고, 이벤트 금액
            assertThat(res).isNotNull();
            assertThat(res.finalAmount()).isEqualTo(expectedTotal);

            ProductModel p1u = productRepository.getProduct(p1.getId()).orElseThrow();
            ProductModel p2u = productRepository.getProduct(p2.getId()).orElseThrow();
            assertAll(
                    () -> assertThat(p1u.getStock()).isEqualTo(5L - 2L),
                    () -> assertThat(p2u.getStock()).isEqualTo(10L - 4L)
            );

            OrderCreatedEvent ev = events.stream(OrderCreatedEvent.class)
                    .findFirst().orElseThrow();
            assertThat(ev.amount()).isEqualTo(expectedTotal);
        }

        @Test
        @DisplayName("쿠폰 사용 주문: 할인 반영 금액으로 이벤트 발행 + 재고 차감")
        void order_with_coupon(ApplicationEvents events) {
            // 쿠폰(정액 5, 유효 +1일, 발급가능 5)
            CouponModel coupon = couponRepository.saveCoupon(
                    new FixedCouponModel("정액5", 5L, ZonedDateTime.now().plusDays(1), 5L)
            );
            UserModel user = userRepository.save(new UserModel(
                    "userB", Gender.FEMALE.getCode(), "1995-05-05", "b@test.com"));
            IssuedCouponModel issued = issuedCouponRepository.saveIssuedCoupon(
                    new IssuedCouponModel(user.getId(), coupon.getId())
            );

            Long BRAND_ID = 1L;
            ProductModel p1 = productRepository.saveProduct(new ProductModel("A", 5L, 5_000L, ProductStatus.SELL, BRAND_ID));
            ProductModel p2 = productRepository.saveProduct(new ProductModel("B", 10L, 1_000L, ProductStatus.SELL, BRAND_ID));

            List<OrderCriteria.ProductQuantity> items = List.of(
                    new OrderCriteria.ProductQuantity(p1.getId(), 2L), // 10_000
                    new OrderCriteria.ProductQuantity(p2.getId(), 4L)  // 4_000
            );
            long total = 14_000L;
            long discount = coupon.calculateDiscount(total);  // 네 구현대로 할인액 반환 가정
            long expectedFinal = total - discount;

            OrderCriteria.Order req = new OrderCriteria.Order(
                    user.getId(), issued.getId(), "주소", "01022223333", "임꺽정",
                    PaymentType.CARD, "SAMSUNG","1111-2222-3333-4444",items
            );

            // when
            OrderInfo.OrderResponse res = orderFacade.order(req);

            // then
            assertThat(res).isNotNull();
            assertThat(res.finalAmount()).isEqualTo(expectedFinal);

            ProductModel p1u = productRepository.getProduct(p1.getId()).orElseThrow();
            ProductModel p2u = productRepository.getProduct(p2.getId()).orElseThrow();
            assertAll(
                    () -> assertThat(p1u.getStock()).isEqualTo(5L - 2L),
                    () -> assertThat(p2u.getStock()).isEqualTo(10L - 4L)
            );

            OrderCreatedEvent ev = events.stream(OrderCreatedEvent.class)
                    .findFirst().orElseThrow();
            assertThat(ev.amount()).isEqualTo(expectedFinal);
        }

        @Test
        @DisplayName("판매가능 상품 수가 맞지 않으면 NOT_FOUND 예외")
        void order_product_mismatch_throws() {
            UserModel user = userRepository.save(new UserModel(
                    "userC", Gender.MALE.getCode(), "2001-01-01", "c@test.com"));

            Long BRAND_ID = 1L;
            // DB에는 한 개만 SELL
            ProductModel p1 = productRepository.saveProduct(new ProductModel("A", 5L, 5_000L, ProductStatus.SELL, BRAND_ID));
            ProductModel p2 = productRepository.saveProduct(new ProductModel("B", 10L, 1_000L, ProductStatus.DISCONTINUED, BRAND_ID));

            List<OrderCriteria.ProductQuantity> items = List.of(
                    new OrderCriteria.ProductQuantity(p1.getId(), 1L),
                    new OrderCriteria.ProductQuantity(p2.getId(), 1L)
            );

            OrderCriteria.Order req = new OrderCriteria.Order(
                    user.getId(), null, "주소", "01033334444", "이몽룡",
                    PaymentType.CARD, "SAMSUNG","1111-2222-3333-4444", items
            );

            CoreException ex = assertThrows(CoreException.class, () -> orderFacade.order(req));
            assertThat(ex.getErrorType()).isEqualTo(ErrorType.NOT_FOUND);
        }


    }

    @Nested
    @DisplayName("결제 실패 보상 플로우")
    class PaymentFailureCompensation {

        @Test
        @DisplayName("failedPayment → PaymentFailedEvent → 재고 복구")
        void payment_failed_compensates(ApplicationEvents events) throws Exception {
            // 1) 주문 생성(쿠폰 사용 + 재고 차감)
            CouponModel coupon = couponRepository.saveCoupon(
                    new FixedCouponModel("정액5", 5L, ZonedDateTime.now().plusDays(1), 5L)
            );
            UserModel user = userRepository.save(new UserModel(
                    "userD", Gender.MALE.getCode(), "1992-02-02", "d@test.com"));
            IssuedCouponModel issued = issuedCouponRepository.saveIssuedCoupon(
                    new IssuedCouponModel(user.getId(), coupon.getId())
            );

            Long BRAND_ID = 1L;
            ProductModel p1 = productRepository.saveProduct(new ProductModel("A", 5L, 5_000L, ProductStatus.SELL, BRAND_ID));
            ProductModel p2 = productRepository.saveProduct(new ProductModel("B", 10L, 1_000L, ProductStatus.SELL, BRAND_ID));

            long p1Init = p1.getStock();
            long p2Init = p2.getStock();

            List<OrderCriteria.ProductQuantity> items = List.of(
                    new OrderCriteria.ProductQuantity(p1.getId(), 2L),
                    new OrderCriteria.ProductQuantity(p2.getId(), 4L)
            );

            OrderCriteria.Order req = new OrderCriteria.Order(
                    user.getId(), issued.getId(), "주소", "01055556666", "성춘향",
                    PaymentType.CARD, "SAMSUNG","1111-2222-3333-4444", items
            );
            orderFacade.order(req);

            Long orderId = events.stream(OrderCreatedEvent.class)
                    .findFirst().map(OrderCreatedEvent::orderId)
                    .orElseThrow();

            // 2) 결제 실패 확정 (REQUIRES_NEW + 이벤트 발행)
            orderFacade.failedPayment(orderId);

            // 3) @Async 리스너 대기 (간단 폴링)
            long deadline = System.currentTimeMillis() + 5000;
            while (System.currentTimeMillis() < deadline) {
                ProductModel a = productRepository.getProduct(p1.getId()).orElseThrow();
                ProductModel b = productRepository.getProduct(p2.getId()).orElseThrow();
                if (a.getStock() == p1Init && b.getStock() == p2Init) break;
                Thread.sleep(100);
            }

            // 4) PaymentFailedEvent 발행 확인
            assertThat(events.stream(PaymentFailedEvent.class).count()).isGreaterThan(0);

            // 5) **재고는 초기값으로 원복되어야 함**
            ProductModel p1After = productRepository.getProduct(p1.getId()).orElseThrow();
            ProductModel p2After = productRepository.getProduct(p2.getId()).orElseThrow();
            assertAll(
                    () -> assertThat(p1After.getStock()).isEqualTo(p1Init),
                    () -> assertThat(p2After.getStock()).isEqualTo(p2Init)
            );
        }
    }
}
