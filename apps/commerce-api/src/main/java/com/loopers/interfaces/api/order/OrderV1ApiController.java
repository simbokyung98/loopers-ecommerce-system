package com.loopers.interfaces.api.order;


import com.loopers.application.order.OrderFacade;
import com.loopers.application.order.dto.OrderCriteria;
import com.loopers.application.order.dto.OrderInfo;
import com.loopers.interfaces.api.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/orders")
public class OrderV1ApiController implements OrderV1ApiSpec {

    private final OrderFacade orderFacade;

    @Override
    @PostMapping
    public ApiResponse<OrderV1Dto.Order> order(@RequestHeader(value = "X-USER-ID") Long userid, @RequestBody OrderV1Dto.OrderRequest request) {

        OrderCriteria.Order criteria = request.toOrder(userid);
        OrderInfo.OrderResponse orderInfo = orderFacade.order(criteria);
        OrderV1Dto.Order orderResponse = OrderV1Dto.Order.from(orderInfo);
        return ApiResponse.success(orderResponse);
    }

    @Override
    @GetMapping
    public ApiResponse<OrderV1Dto.UserOrdersResponse> getOrdersByUserId(@RequestHeader(value = "X-USER-ID") Long userid) {
        OrderInfo.UserOrders userOrders = orderFacade.getOrdersByUserId(userid);
        OrderV1Dto.UserOrdersResponse userOrdersResponse = OrderV1Dto.UserOrdersResponse.from(userOrders);
        return ApiResponse.success(userOrdersResponse);
    }
}

