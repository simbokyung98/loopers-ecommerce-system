package com.loopers.application.order;

import com.loopers.application.order.dto.OrderCriteria;
import com.loopers.application.order.dto.OrderInfo;
import com.loopers.application.payment.PaymentEventHandler; // 주문 생성 이벤트 핸들러(테스트 간섭 방지용 Mock)
import com.loopers.domain.coupon.CouponRepository;
import com.loopers.domain.coupon.IssuedCouponRepository;
import com.loopers.domain.coupon.model.CouponModel;
import com.loopers.domain.coupon.model.FixedCouponModel;
import com.loopers.domain.coupon.model.IssuedCouponModel;
import com.loopers.domain.order.OrderResult;
import com.loopers.domain.order.OrderService;
import com.loopers.domain.payment.PaymentType;
import com.loopers.domain.payment.event.PaymentFailedEvent;
import com.loopers.domain.product.ProductCommand;
import com.loopers.domain.product.ProductModel;
import com.loopers.domain.product.ProductRepository;
import com.loopers.domain.product.ProductService;
import com.loopers.domain.product.ProductStatus;
import com.loopers.domain.user.UserModel;
import com.loopers.domain.user.UserRepository;
import com.loopers.interfaces.api.User.Gender;
import com.loopers.utils.DatabaseCleanUp;
import com.loopers.utils.RedisCleanUp;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatcher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.context.event.ApplicationEvents;
import org.springframework.test.context.event.RecordApplicationEvents;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

@SpringBootTest
@RecordApplicationEvents
class OrderEventHandlerIntegrationTest {

    @Autowired
    private OrderFacade orderFacade;
    @Autowired
    private OrderService orderService;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private CouponRepository couponRepository;
    @Autowired
    private IssuedCouponRepository issuedCouponRepository;
    @Autowired
    private DatabaseCleanUp databaseCleanUp;
    @Autowired
    private RedisCleanUp redisCleanUp;


    @MockitoBean
    private PaymentEventHandler paymentEventHandler;


    @MockitoSpyBean
    private ProductService productService;
    @MockitoSpyBean
    private com.loopers.domain.coupon.CouponService couponService;

    private static final Long BRAND_ID = 1L;

    @AfterEach
    void tearDown() {
        databaseCleanUp.truncateAllTables();
        redisCleanUp.truncateAll();
    }

    @Test
    @DisplayName("PaymentFailedEvent 처리: 쿠폰 사용 주문이면 재고와 쿠폰이 복구된다")
    void onPaymentFailed_restores_stock_and_coupon(ApplicationEvents events) {
        // given
        UserModel user = userRepository.save(new UserModel("u1", Gender.MALE.getCode(), "1990-01-01", "u1@test.com"));

        ProductModel p1 = productRepository.saveProduct(new ProductModel("A", 5L, 5_000L, ProductStatus.SELL, BRAND_ID));
        ProductModel p2 = productRepository.saveProduct(new ProductModel("B", 10L, 1_000L, ProductStatus.SELL, BRAND_ID));
        long p1Initial = p1.getStock();
        long p2Initial = p2.getStock();

        CouponModel coupon = couponRepository.saveCoupon(
                new FixedCouponModel("정액5", 5L, ZonedDateTime.now().plusDays(1), 5L)
        );
        IssuedCouponModel issued = issuedCouponRepository.saveIssuedCoupon(
                new IssuedCouponModel(user.getId(), coupon.getId())
        );

        List<OrderCriteria.ProductQuantity> items = List.of(
                new OrderCriteria.ProductQuantity(p1.getId(), 2L), // -2
                new OrderCriteria.ProductQuantity(p2.getId(), 4L)  // -4
        );

        OrderCriteria.Order req = new OrderCriteria.Order(
                user.getId(), issued.getId(),
                "주소", "01011112222", "홍길동",
                PaymentType.CARD, "SAMSUNG", "1111-2222-3333-4444",
                items
        );
        OrderInfo.OrderResponse placed = orderFacade.order(req);


        ProductModel afterP1 = productRepository.getProduct(p1.getId()).orElseThrow();
        ProductModel afterP2 = productRepository.getProduct(p2.getId()).orElseThrow();
        assertThat(afterP1.getStock()).isEqualTo(p1Initial - 2L);
        assertThat(afterP2.getStock()).isEqualTo(p2Initial - 4L);

        // when
        orderFacade.failedPayment(placed.orderId());

        // then
        await().atMost(Duration.ofSeconds(4)).untilAsserted(() -> {

            assertThat(events.stream(PaymentFailedEvent.class).count()).isGreaterThan(0L);


            ProductModel restoredP1 = productRepository.getProduct(p1.getId()).orElseThrow();
            ProductModel restoredP2 = productRepository.getProduct(p2.getId()).orElseThrow();
            assertThat(restoredP1.getStock()).isEqualTo(p1Initial);
            assertThat(restoredP2.getStock()).isEqualTo(p2Initial);


            ArgumentMatcher<ProductCommand.ProductQuantity> p1Matcher =
                    pq -> pq != null && pq.productId().equals(p1.getId()) && pq.quantity() == 2L;
            ArgumentMatcher<ProductCommand.ProductQuantity> p2Matcher =
                    pq -> pq != null && pq.productId().equals(p2.getId()) && pq.quantity() == 4L;

            verify(productService, atLeast(1)).restoreStock(argThat(p1Matcher));
            verify(productService, atLeast(1)).restoreStock(argThat(p2Matcher));


            verify(couponService, atLeast(1)).restoreCoupon(issued.getId());
        });


        OrderResult.Order orderView = orderService.getOrderDetailById(placed.orderId());
        assertThat(orderView.status().name()).isEqualTo("PAYMENT_FAILED");
    }

    @Test
    @DisplayName("PaymentFailedEvent 처리: 쿠폰 미사용 주문이면 재고만 복구되고 쿠폰 복구는 호출되지 않는다")
    void onPaymentFailed_restores_only_stock_when_no_coupon(ApplicationEvents events) {
        // given
        UserModel user = userRepository.save(new UserModel("u2", Gender.FEMALE.getCode(), "1991-01-01", "u2@test.com"));
        ProductModel p = productRepository.saveProduct(new ProductModel("P", 3L, 2_000L, ProductStatus.SELL, BRAND_ID));
        long initial = p.getStock();

        List<OrderCriteria.ProductQuantity> items = List.of(
                new OrderCriteria.ProductQuantity(p.getId(), 2L) // -2
        );
        OrderCriteria.Order req = new OrderCriteria.Order(
                user.getId(), null,
                "주소", "01022223333", "임꺽정",
                PaymentType.CARD, "SAMSUNG", "1111-2222-3333-4444",
                items
        );
        OrderInfo.OrderResponse placed = orderFacade.order(req);

        ProductModel after = productRepository.getProduct(p.getId()).orElseThrow();
        assertThat(after.getStock()).isEqualTo(initial - 2L);

        // when
        orderFacade.failedPayment(placed.orderId());

        // then
        await().atMost(Duration.ofSeconds(4)).untilAsserted(() -> {
            assertThat(events.stream(PaymentFailedEvent.class).count()).isGreaterThan(0L);

            ProductModel restored = productRepository.getProduct(p.getId()).orElseThrow();
            assertThat(restored.getStock()).isEqualTo(initial);

            // 재고 복구 호출은 있음
            verify(productService, atLeast(1)).restoreStock(any(ProductCommand.ProductQuantity.class));
            // 쿠폰 복구 호출은 없음
            verify(couponService, never()).restoreCoupon(anyLong());
        });

        OrderResult.Order orderView = orderService.getOrderDetailById(placed.orderId());
        assertThat(orderView.status().name()).isEqualTo("PAYMENT_FAILED");
    }
}
