package com.minimall.api;

import com.minimall.domain.embeddable.Address;
import com.minimall.domain.embeddable.AddressDto;
import com.minimall.domain.embeddable.AddressMapper;
import com.minimall.domain.order.delivery.dto.DeliverySummaryDto;
import com.minimall.domain.order.dto.request.OrderCreateRequestDto;
import com.minimall.domain.order.dto.response.OrderCreateResponseDto;
import com.minimall.domain.order.dto.response.OrderDetailResponseDto;
import com.minimall.domain.order.pay.dto.PayRequestDto;
import com.minimall.domain.order.pay.dto.PaySummaryDto;
import com.minimall.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.Nullable;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/orders", produces = "application/json")
@Tag(name = "Order API", description = "주문 관련 API")
public class OrderController {

    private final OrderService orderService;
    private final AddressMapper addressMapper;

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

    @Operation(summary = "주문 단건 상세 조회")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "404", description = "주문 미존재")
    })
    @GetMapping("/{id}")
    public ResponseEntity<OrderDetailResponseDto> getOrder(@PathVariable Long id) {
        OrderDetailResponseDto response = orderService.getOrderDetail(id);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "주문 결제 처리")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "결제 성공"),
            @ApiResponse(responseCode = "422", description = "주문 혹은 결제 상태 오류 or 주문 금액, 결제 금액 불일치")
    })
    @PostMapping("/{id}/payment")
    public ResponseEntity<PaySummaryDto> processPayment(@PathVariable Long id, @RequestBody PayRequestDto request) {
        PaySummaryDto body = orderService.processPayment(id, request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest().build().toUri();
        return ResponseEntity.created(location).body(body);
    }

    @Operation(summary = "배송 준비")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "배송 준비 성공"),
            @ApiResponse(responseCode = "422", description = "배송 상태 오류 or 배송 주소와 회원 주소 모두 누락")
    })
    @PostMapping("/{id}/delivery")
    public ResponseEntity<DeliverySummaryDto> prepareDelivery(@PathVariable Long id,
                                                              @RequestBody(required = false) AddressDto shipAddrDto) {

        Address shipAddr = (shipAddrDto != null) ? addressMapper.toEntity(shipAddrDto) : null;
        DeliverySummaryDto body = orderService.prepareDelivery(id, shipAddr);

        URI location = ServletUriComponentsBuilder.fromCurrentRequest().build().toUri();
        return ResponseEntity.created(location).body(body);
    }
}