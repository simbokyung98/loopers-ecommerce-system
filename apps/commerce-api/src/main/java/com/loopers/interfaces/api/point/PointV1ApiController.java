package com.loopers.interfaces.api.point;

import com.loopers.application.point.PointFacade;
import com.loopers.interfaces.api.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;


@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/points")
public class PointV1ApiController implements PointV1ApiSpec{

    private final PointFacade pointFacade;
    @Override
    @PostMapping("/charge")
    public ApiResponse<Long> charge(@RequestHeader("X-USER-ID") Long userId, @RequestBody PointV1Dto.PointRequest pointRequest) {
        return ApiResponse.success(pointFacade.charge(userId, pointRequest.point()));
    }

    @Override
    @GetMapping
    public ApiResponse<Long> get(@RequestHeader(value = "X-USER-ID") Long userId) {

        return ApiResponse.success(pointFacade.getPointAmount(userId));
    }
}
