package com.loopers.application.order;


import com.loopers.application.order.dto.OrderCriteria;
import com.loopers.application.order.dto.OrderInfo;
import com.loopers.domain.coupon.CouponRepository;
import com.loopers.domain.coupon.IssuedCouponRepository;
import com.loopers.domain.coupon.model.CouponModel;
import com.loopers.domain.coupon.model.FixedCouponModel;
import com.loopers.domain.coupon.model.IssuedCouponModel;
import com.loopers.domain.point.PointModel;
import com.loopers.domain.point.PointRepository;
import com.loopers.domain.product.ProductModel;
import com.loopers.domain.product.ProductRepository;
import com.loopers.domain.product.ProductStatus;
import com.loopers.domain.user.UserModel;
import com.loopers.domain.user.UserRepository;
import com.loopers.interfaces.api.User.Gender;
import com.loopers.utils.DatabaseCleanUp;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.ZonedDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

@SpringBootTest
public class OrderFacadeIntegrationTest {

    @Autowired
    private OrderFacade orderFacade;
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private PointRepository pointRepository;

    @Autowired
    private CouponRepository couponRepository;

    @Autowired
    private IssuedCouponRepository issuedCouponRepository;

    @Autowired
    private DatabaseCleanUp databaseCleanUp;

    @AfterEach
    void tearDown() {
        databaseCleanUp.truncateAllTables();
    }


    @DisplayName("주문할 떄,")
    @Nested
    class Order {

        @Test
        @DisplayName("주문 요청 시, 포인트 차감 및 주문이 생성된다")
        void order_success(){
            CouponModel coupon = couponRepository.saveCoupon(
                    new FixedCouponModel("정상 쿠폰", 5L, ZonedDateTime.now().plusDays(1), 5L)
            );

            UserModel requestModel = new UserModel(
                    "testId",
                    Gender.MALE.getCode(),
                    "2024-05-22",
                    "loopers@test.com"
            );
            UserModel userModel =  userRepository.save(requestModel);

            IssuedCouponModel issuedCouponModel = issuedCouponRepository.saveIssuedCoupon(
                    new IssuedCouponModel(userModel.getId(), coupon.getId())
            );

            Long chargePoint = 20_000L;
            PointModel pointModel = new PointModel(userModel.getId());
            pointModel.charge(chargePoint);
            pointRepository.save(pointModel);


            Long BRAND_ID = 1L;

            ProductModel p1 =
                    new ProductModel(
                            "테스트 상품",
                            5L,
                            5_000L,
                            ProductStatus.SELL, BRAND_ID);
            ProductModel p2 =
                    new ProductModel(
                            "루퍼스 상품",
                            10L,
                            1_000L,
                            ProductStatus.SELL, BRAND_ID);
            ProductModel product1 = productRepository.saveProduct(p1);
            ProductModel product2 = productRepository.saveProduct(p2);


            List<OrderCriteria.ProductQuantity> productQuantity = List.of(
                    new OrderCriteria.ProductQuantity(product1.getId(), 2L),
                    new OrderCriteria.ProductQuantity(product2.getId(), 4L)
            );

            OrderCriteria.Order orderRequest = new OrderCriteria.Order(
                    userModel.getId(),
                    issuedCouponModel.getId(),
                    "테스트 주소",
                    "01011112222",
                    "홍길동",
                    productQuantity
                    );

            //act
            OrderInfo.OrderResponse result = orderFacade.order(orderRequest);

            long discountAmount = coupon.calculateDiscount(14_000L);
            //assert
            assertAll(
                    () -> assertThat(result).isNotNull(),
                    () ->assertThat(result.totalAmount()).isEqualTo(discountAmount)
            );

            PointModel pointResult = pointRepository.findByUserId(userModel.getId()).orElseThrow();
            assertThat(pointResult.getTotalAmount()).isEqualTo(chargePoint - discountAmount);

            ProductModel updateProduct = productRepository.getProduct(product1.getId()).orElseThrow();
            assertThat(updateProduct.getStock()).isEqualTo(3L);

        }



    }
}
