package com.loopers.interfaces.api.order;


import com.loopers.application.order.OrderFacade;
import com.loopers.application.order.dto.OrderInfo;
import com.loopers.application.purchase.PurchaseCriteria;
import com.loopers.application.purchase.PurchaseFacade;
import com.loopers.interfaces.api.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v2/orders")
public class OrderV2ApiController implements OrderV2ApiSpec {

    private final OrderFacade orderFacade;
    private final PurchaseFacade purchaseFacade;
    @Override
    @PostMapping
    public ApiResponse<OrderV2Dto.Order> order(@RequestHeader(value = "X-USER-ID") Long userid, @RequestBody OrderV2Dto.OrderRequest request) {

        PurchaseCriteria.Purchase criteria = request.toPurchase(userid);
        OrderInfo.OrderResponse orderInfo = purchaseFacade.purchase(criteria);
        OrderV2Dto.Order orderResponse = OrderV2Dto.Order.from(orderInfo);
        return ApiResponse.success(orderResponse);
    }

}

