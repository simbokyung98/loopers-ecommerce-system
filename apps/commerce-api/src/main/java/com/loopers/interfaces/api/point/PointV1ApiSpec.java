package com.loopers.interfaces.api.point;

import com.loopers.interfaces.api.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

@Tag(name = "Point V1 API", description = "Point API 입니다.")
public interface PointV1ApiSpec {

    @Operation(summary = "포인트 충전")
    ApiResponse<Long> charge(
            @RequestHeader("X-USER-ID") Long userid,
            @RequestBody PointV1Dto.PointRequest pointRequest
    );
}
