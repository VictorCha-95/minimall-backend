package com.minimall.api.order;

import com.minimall.api.order.delivery.dto.DeliverySummaryResponse;
import com.minimall.api.order.delivery.dto.StartDeliveryRequest;
import com.minimall.api.order.dto.OrderMapper;
import com.minimall.api.order.dto.request.CompleteDeliveryRequest;
import com.minimall.api.order.pay.dto.PayMapper;
import com.minimall.domain.embeddable.Address;
import com.minimall.api.common.embeddable.AddressDto;
import com.minimall.api.common.embeddable.AddressMapper;
import com.minimall.api.order.dto.request.OrderCreateRequest;
import com.minimall.api.order.dto.response.OrderCreateResponse;
import com.minimall.api.order.dto.response.OrderDetailResponse;
import com.minimall.api.order.pay.dto.PayRequest;
import com.minimall.api.order.pay.dto.PayResponse;
import com.minimall.domain.order.Order;
import com.minimall.domain.order.Pay;
import com.minimall.service.order.dto.OrderItemCreateCommand;
import com.minimall.service.order.OrderService;
import com.minimall.service.order.dto.OrderCreateCommand;
import com.minimall.service.order.dto.PayCommand;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/orders", produces = "application/json")
@Tag(name = "Order API", description = "주문 관련 API")
public class OrderController {

    private final OrderService orderService;
    private final OrderMapper orderMapper;
    private final PayMapper payMapper;
    private final AddressMapper addressMapper;

    @Operation(summary = "주문 생성")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "생성 성공"),
            @ApiResponse(responseCode = "400", description = "요청 검증 오류"),
            @ApiResponse(responseCode = "404", description = "회원/상품 미존재")
    })
    @PostMapping
    public ResponseEntity<OrderCreateResponse> createOrder(@Valid @RequestBody OrderCreateRequest request) {

        OrderCreateCommand command = new OrderCreateCommand(
                request.memberId(),
                request.items().stream()
                        .map(i -> new OrderItemCreateCommand(i.productId(), i.quantity()))
                        .toList()
        );

        Order order = orderService.createOrder(command);
        OrderCreateResponse body = orderMapper.toCreateResponse(order);

        return ResponseEntity
                .created(URI.create("/orders/" + body.id()))
                .body(body);
    }

    @Operation(summary = "주문 단건 상세 조회")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "400", description = "요청 검증 오류"),
            @ApiResponse(responseCode = "404", description = "주문 미존재")
    })
    @GetMapping("/{id}")
    public ResponseEntity<OrderDetailResponse> getOrder(@PathVariable Long id) {
        OrderDetailResponse response = orderService.getOrderDetail(id);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "주문 결제 처리")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "결제 성공"),
            @ApiResponse(responseCode = "400", description = "요청 검증 오류"),
            @ApiResponse(responseCode = "422", description = "주문 혹은 결제 상태 오류 or 주문 금액, 결제 금액 불일치")
    })
    @PostMapping("/{id}/payment")
    public ResponseEntity<PayResponse> processPayment(@PathVariable Long id,
                                                      @Valid @RequestBody PayRequest request) {
        PayCommand command = new PayCommand(request.payMethod(), request.payAmount());
        Pay pay = orderService.processPayment(id, command);
        PayResponse body = payMapper.toPaySummary(pay);
        return ResponseEntity.ok(body);
    }

    @Operation(summary = "배송 준비")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "배송 준비 성공"),
            @ApiResponse(responseCode = "400", description = "요청 검증 오류"),
            @ApiResponse(responseCode = "404", description = "주문 미존재"),
            @ApiResponse(responseCode = "422", description = "배송 상태 오류 or 배송 주소와 회원 주소 모두 누락")
    })
    @PostMapping("/{id}/delivery")
    public ResponseEntity<DeliverySummaryResponse> prepareDelivery(@PathVariable Long id,
                                                                   @RequestBody(required = false) AddressDto shipAddrDto) {

        Address shipAddr = (shipAddrDto != null) ? addressMapper.toEntity(shipAddrDto) : null;
        DeliverySummaryResponse body = orderService.prepareDelivery(id, shipAddr);

        URI location = ServletUriComponentsBuilder.fromCurrentRequest().build().toUri();
        return ResponseEntity.created(location).body(body);
    }

    @Operation(summary = "배송 시작")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "배송 시작 성공"),
            @ApiResponse(responseCode = "400", description = "요청 검증 오류"),
            @ApiResponse(responseCode = "404", description = "주문 미존재"),
            @ApiResponse(responseCode = "422", description = "배송 상태 오류")
    })
    @PatchMapping("/{id}/delivery")
    public ResponseEntity<Void> startDelivery(@PathVariable Long id,
                                              @Valid @RequestBody StartDeliveryRequest request) {

        orderService.startDelivery(id, request.trackingNo(), request.shippedAt());
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "배송 완료")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "배송 시작 성공"),
            @ApiResponse(responseCode = "400", description = "요청 검증 오류"),
            @ApiResponse(responseCode = "404", description = "주문 미존재"),
            @ApiResponse(responseCode = "422", description = "배송 상태 오류")
    })
    @PatchMapping("/{id}/delivery/complete")
    public ResponseEntity<Void> completeDelivery(@PathVariable Long id,
                                                 @Valid @RequestBody CompleteDeliveryRequest request) {

        orderService.completeDelivery(id, request.arrivedAt());
        return ResponseEntity.noContent().build();
    }
}