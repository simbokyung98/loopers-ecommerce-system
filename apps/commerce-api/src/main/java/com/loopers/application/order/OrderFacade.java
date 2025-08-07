package com.loopers.application.order;


import com.loopers.application.order.dto.OrderCriteria;
import com.loopers.application.order.dto.OrderInfo;
import com.loopers.domain.coupon.CouponService;
import com.loopers.domain.order.OrderCommand;
import com.loopers.domain.order.OrderModel;
import com.loopers.domain.order.OrderService;
import com.loopers.domain.point.PointService;
import com.loopers.domain.product.ProductService;
import com.loopers.domain.product.ProductSnapshotResult;
import com.loopers.domain.user.UserService;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class OrderFacade {

    private final UserService userService;
    private final ProductService productService;
    private final OrderService orderService;
    private final PointService pointService;
    private final CouponService couponService;

    @Transactional
    public OrderInfo.Order order(OrderCriteria.Order criteria){

        //유저 체크
        userService.checkExistUser(criteria.userId());

        Map<Long, OrderCriteria.ProductQuantity> productQuantityMap =
               criteria.productQuantities().stream().collect(Collectors.toMap(OrderCriteria.ProductQuantity::productId, Function.identity()));

        List<ProductSnapshotResult> productModels = productService.getProductsForSnapshot(productQuantityMap.keySet().stream().toList());

        //상품 체크
        if(productQuantityMap.size() != productModels.size()){
            throw new CoreException(ErrorType.NOT_FOUND, "주문할 수 없는 상품이 있습니다.");
        }

        //주문 아이템 생성
        List<OrderCommand.Product> commandProducts = new ArrayList<>();

        for(ProductSnapshotResult productSnapshotResult : productModels){
//            productModel.validateSellable();
            OrderCriteria.ProductQuantity productQuantity = productQuantityMap.get(productSnapshotResult.id());

            commandProducts.add(new OrderCommand.Product(
                    productSnapshotResult.id(),
                    productSnapshotResult.name(),
                    productSnapshotResult.price(),
                    productQuantity.quantity()
            ));
        }

        //총금액계산
        long totalAmount = OrderAmountCalculator.calculateTotalAmount(commandProducts);

        //쿠폰사용
        if(criteria.issueCouponId() != null){
            totalAmount = couponService.useCoupon(criteria.userId(), criteria.issueCouponId(), totalAmount);
        }

        //포인트 차감
        pointService.spend(criteria.userId(), totalAmount);

        //재고차감
        productService.deductStocks(criteria.toDeductStocks());

        //주문 생성
        OrderCommand.PlaceOrder placeOrder = criteria.toCommand(totalAmount, commandProducts);
        OrderModel orderModel = orderService.placeOrder(placeOrder);

        return OrderInfo.Order.from(orderModel);

    }
}
