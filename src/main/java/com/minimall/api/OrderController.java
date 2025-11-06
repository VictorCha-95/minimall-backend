package com.minimall.api;

import com.minimall.domain.order.dto.request.OrderCreateRequestDto;
import com.minimall.domain.order.dto.response.OrderCreateResponseDto;
import com.minimall.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;

@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/orders", produces = "application/json")
@Tag(name = "Order API", description = "주문 관련 API")
public class OrderController {

    private final OrderService orderService;

    @Operation(summary = "주문 생성")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "생성 성공"),
            @ApiResponse(responseCode = "400", description = "요청 검증 오류"),
            @ApiResponse(responseCode = "404", description = "회원/상품 미존재")
    })
    @PostMapping
    public ResponseEntity<OrderCreateResponseDto> create(@Valid @RequestBody OrderCreateRequestDto request) {
        OrderCreateResponseDto response = orderService.createOrder(request);
        return ResponseEntity.created(URI.create("/orders/" + response.id())).body(response);
    }
}
