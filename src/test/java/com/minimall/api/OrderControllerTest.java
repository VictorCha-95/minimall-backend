package com.minimall.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.minimall.api.order.OrderController;
import com.minimall.api.order.delivery.dto.DeliveryApiMapper;
import com.minimall.api.order.delivery.dto.DeliverySummaryResponse;
import com.minimall.api.order.delivery.dto.StartDeliveryRequest;
import com.minimall.api.order.dto.OrderApiMapper;
import com.minimall.api.order.dto.request.CompleteDeliveryRequest;
import com.minimall.api.order.dto.response.OrderCreateResponse;
import com.minimall.api.order.dto.response.OrderDetailResponse;
import com.minimall.api.order.dto.response.OrderItemResponse;
import com.minimall.api.order.pay.dto.PayApiMapper;
import com.minimall.api.order.pay.dto.PayResponse;
import com.minimall.domain.embeddable.Address;
import com.minimall.api.common.embeddable.AddressDto;
import com.minimall.api.common.embeddable.AddressMapper;
import com.minimall.domain.embeddable.InvalidAddressException;
import com.minimall.domain.order.Order;
import com.minimall.domain.order.OrderStatus;
import com.minimall.domain.order.Pay;
import com.minimall.domain.order.delivery.DeliveryException;
import com.minimall.domain.order.delivery.DeliveryStatus;
import com.minimall.api.order.dto.request.OrderCreateRequest;
import com.minimall.api.order.dto.request.OrderItemCreateRequest;
import com.minimall.domain.order.delivery.DeliveryStatusException;
import com.minimall.domain.order.pay.PayStatus;
import com.minimall.domain.order.exception.OrderStatusException;
import com.minimall.domain.order.exception.PaymentRequiredException;
import com.minimall.domain.order.pay.PayAmountMismatchException;
import com.minimall.domain.order.pay.PayMethod;
import com.minimall.api.order.pay.dto.PayRequest;
import com.minimall.service.order.OrderService;
import com.minimall.service.exception.MemberNotFoundException;
import com.minimall.service.exception.OrderNotFoundException;
import com.minimall.service.exception.ProductNotFoundException;
import com.minimall.service.order.dto.command.OrderCreateCommand;
import com.minimall.service.order.dto.command.PayCommand;
import com.minimall.service.order.dto.result.DeliverySummaryResult;
import com.minimall.service.order.dto.result.OrderDetailResult;
import com.minimall.service.order.dto.result.OrderItemResult;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.BDDMockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@WebMvcTest(OrderController.class)
public class OrderControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @MockitoBean
    OrderApiMapper orderApiMapper;

    @MockitoBean
    DeliveryApiMapper deliveryApiMapper;

    @MockitoBean
    PayApiMapper payApiMapper;

    @MockitoBean
    AddressMapper addressMapper;

    @MockitoBean
    OrderService orderService;

    private OrderCreateRequest createRequest;
    private OrderDetailResult detailResult;
    private OrderDetailResponse detailResponse;

    private static final long NOT_EXIST_ID = 999_999_999L;

    @BeforeEach
    void setUp() {
        createRequest = new OrderCreateRequest(
                1L,
                List.of(new OrderItemCreateRequest(1L, 10),
                        new OrderItemCreateRequest(2L, 20)));

        detailResult = new OrderDetailResult(
                1L,
                LocalDateTime.of(2025, 11, 11, 12, 30),
                OrderStatus.ORDERED,
                100_000,
                List.of(new OrderItemResult(
                        1L,
                        "도서",
                        10_000,
                        10,
                        100_000)),
                null, null);

        detailResponse = new OrderDetailResponse(
                1L,
                LocalDateTime.of(2025, 11, 11, 12, 30),
                OrderStatus.ORDERED,
                100_000,
                List.of(new OrderItemResponse(
                        1L,
                        "도서",
                        10_000,
                        10,
                        100_000)),
                null, null);
    }

    @Nested
    @DisplayName("POST /orders")
    class CreateOrder {
        @Test
        @DisplayName("주문 생성 -> 201 + JSON 검증")
        void return201_whenSuccess() throws Exception {
            //given
            Order sutbOrder = mock(Order.class);
            given(orderService.createOrder(any(OrderCreateCommand.class)))
                    .willReturn(sutbOrder);

            given(orderApiMapper.toCreateResponse(sutbOrder))
                    .willReturn(new OrderCreateResponse(
                            1L,
                            LocalDateTime.now(),
                            OrderStatus.ORDERED,
                            100_000, 0, 100_000,
                            2
                    ));

            //when
            ResultActions result = mockMvc.perform(post("/orders")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(createRequest)));

            //then
            result.andExpect(status().isCreated())
                    .andExpect(header().string("Location", Matchers.endsWith("/orders/1")))
                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.id").value(1))
                    .andExpect(jsonPath("$.finalAmount").value(100_000))
                    .andExpect(jsonPath("$.orderStatus").value("ORDERED"))
                    .andExpect(jsonPath("$.itemCount").value(2));

            then(orderService).should(times(1)).createOrder(any(OrderCreateCommand.class));
            then(orderApiMapper).should(times(1)).toCreateResponse(any(Order.class));
        }

        @Test
        @DisplayName("요청 형식 오류 -> 400 Bad Request")
        void return400_whenInvalidPayload() throws Exception {
            //given: memberId 누락 + quantity 0
            String invalidJson = """
                    { "items": [ { "productId": 1, "quantity": 0 } ] } 
                    """;

            //when
            ResultActions result = mockMvc.perform(post("/orders")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(invalidJson));

            //then
            result.andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status").value(400))
                    .andExpect(jsonPath("$.errorCode").value("VALIDATION_ERROR"))
                    .andExpect(jsonPath("$.path").value("/orders"))
                    .andExpect(jsonPath("$.message").exists());

            verifyNoInteractions(orderService);
        }


        @Test
        @DisplayName("회원 미존재 -> 404 Not Found")
        void return404_whenMemberNotFound() throws Exception{
            //given
            given(orderService.createOrder(any())).willThrow(new MemberNotFoundException("id", NOT_EXIST_ID));

            //when
            ResultActions result = mockMvc.perform(post("/orders")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(createRequest)));

            //then
            result.andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.status").value(404))
                    .andExpect(jsonPath("$.errorCode").value("NOT_FOUND"))
                    .andExpect(jsonPath("$.path").value("/orders"))
                    .andExpect(jsonPath("$.message", Matchers.containsString("회원")))
                    .andExpect(jsonPath("$.message", Matchers.containsString("id")))
                    .andExpect(jsonPath("$.message", Matchers.containsString(String.valueOf(NOT_EXIST_ID))))
                    .andExpect(jsonPath("$.timestamp").exists());
        }

        @Test
        @DisplayName("상품 미존재 -> 404 Not Found")
        void return404_whenProductNotFound() throws Exception{
            //given
            given(orderService.createOrder(any())).willThrow(new ProductNotFoundException("id", NOT_EXIST_ID));

            //when
            ResultActions result = mockMvc.perform(post("/orders")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(createRequest)));

            //then
            result.andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.status").value(404))
                    .andExpect(jsonPath("$.errorCode").value("NOT_FOUND"))
                    .andExpect(jsonPath("$.path").value("/orders"))
                    .andExpect(jsonPath("$.message", Matchers.containsString("상품")))
                    .andExpect(jsonPath("$.message", Matchers.containsString("id")))
                    .andExpect(jsonPath("$.message", Matchers.containsString(String.valueOf(NOT_EXIST_ID))))
                    .andExpect(jsonPath("$.timestamp").exists());
        }
    }

    @Nested
    @DisplayName("PATCH /orders/{id}/cancel")
    class CancelOrder {
        @DisplayName("주문 취소: 204 검증")
        @Test
        void return204_whenSuccess() throws Exception {
            //when
            ResultActions result = mockMvc.perform(patch("/orders/1/cancel"));

            //then
            result.andExpect(status().isNoContent());
        }

        @DisplayName("주문 미존재 -> 404 NotFound 예외 발생")
        @Test
        void return404_whenOrderNotFound() throws Exception {
            //given
            willThrow(new OrderNotFoundException("id", NOT_EXIST_ID))
                    .given(orderService).cancelOrder(NOT_EXIST_ID);

            //when
            ResultActions result = mockMvc.perform(patch("/orders/" + NOT_EXIST_ID + "/cancel"));

            //then
            result.andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.status").value(404))
                    .andExpect(jsonPath("$.errorCode").value("NOT_FOUND"))
                    .andExpect(jsonPath("$.message", Matchers.containsString("주문")))
                    .andExpect(jsonPath("$.message", Matchers.containsString("id")))
                    .andExpect(jsonPath("$.message", Matchers.containsString(String.valueOf(NOT_EXIST_ID))))
                    .andExpect(jsonPath("$.timestamp").exists());

        }
    }


    @Nested
    @DisplayName("GET /orders/{id}")
    class GetOrderDetail {
        @Test
        @DisplayName("주문 단건 상세 조회 -> 200 + JSON 검증")
        void return200_whenSuccess() throws Exception {
            //given
            given(orderService.getOrderDetail(1L)).willReturn(detailResult);
            given(orderApiMapper.toOrderDetailResponse(detailResult)).willReturn(detailResponse);

            //when
            ResultActions result = mockMvc.perform(get("/orders/1"));

            //then
            result.andExpect(status().isOk())
                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.id").value(1L));
        }

        @Test
        @DisplayName("주문 없음 -> 404 Not Found")
        void return404_whenOrderNotFound() throws Exception {
            //given
            willThrow(new OrderNotFoundException("id", NOT_EXIST_ID))
                    .given(orderService).getOrderDetail(NOT_EXIST_ID);

            //when
            ResultActions result = mockMvc.perform(get("/orders/" + NOT_EXIST_ID));

            //then
            result.andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.status").value(404))
                    .andExpect(jsonPath("$.errorCode").value("NOT_FOUND"))
                    .andExpect(jsonPath("$.path").value("/orders/" + NOT_EXIST_ID))
                    .andExpect(jsonPath("$.message", Matchers.containsString("주문")))
                    .andExpect(jsonPath("$.message", Matchers.containsString("id")))
                    .andExpect(jsonPath("$.message", Matchers.containsString(String.valueOf(NOT_EXIST_ID))))
                    .andExpect(jsonPath("$.timestamp").exists());
        }
    }

    @Nested
    @DisplayName("POST /orders/{id}/payment")
    class ProcessPayment {
        @Test
        @DisplayName("주문 결제 처리 -> 201 + Location 헤더 + JSON 검증")
        void success() throws Exception{
            //given
            long orderId = 123L;
            PayRequest request = new PayRequest(PayMethod.CARD, 100_000);

            Pay pay = new Pay(
                    PayMethod.CARD,
                    100_000);

            given(orderService.processPayment(eq(orderId), any(PayCommand.class)))
                    .willReturn(pay);

            given(payApiMapper.toPaySummary(pay))
                    .willReturn(new PayResponse(
                            PayMethod.CARD,
                            100_000,
                            PayStatus.PAID,
                            LocalDateTime.of(2025, 11, 14, 12, 30)
                    ));

            //when
            ResultActions result = mockMvc.perform(post("/orders/" + orderId + "/payment")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)));

            //then
            result.andExpect(status().isOk())
                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.payAmount").value(100_000))
                    .andExpect(jsonPath("$.payStatus").value("PAID"));
        }

        @Test
        @DisplayName("중복 결제 -> 422 Unprocessable Entity")
        void shouldFail_whenDuplicatedPay() throws Exception{
            //given
            long orderId = 123L;
            PayRequest request = new PayRequest(PayMethod.CARD, 100_000);

            Pay pay = new Pay(
                    PayMethod.CARD,
                    100_000);

            given(orderService.processPayment(eq(orderId), any(PayCommand.class)))
                    .willReturn(pay)
                    .willThrow(OrderStatusException.class);

            // when-then(1): 첫 결제 성공 -> 201
            mockMvc.perform(post("/orders/{id}/payment", orderId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk());

            // when-then(2): 동일 요청 재시도 -> 422
            mockMvc.perform(post("/orders/{id}/payment", orderId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isUnprocessableEntity())
                    .andExpect(jsonPath("$.status").value(422));
        }

        @Test
        @DisplayName("결제 금액 오류 -> 422 Unprocessable Entity")
        void shouldFail_whenMismatchAmount() throws Exception{
            //given
            long orderId = 123L;
            PayRequest request = new PayRequest(PayMethod.CARD, 100_000);

            given(orderService.processPayment(eq(orderId), any(PayCommand.class)))
                    .willThrow(PayAmountMismatchException.class);

            // when-then
            mockMvc.perform(post("/orders/{id}/payment", orderId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isUnprocessableEntity())
                    .andExpect(jsonPath("$.status").value(422));
        }
    }

    @Nested
    @DisplayName("POST /orders/{id}/delivery")
    class PrepareDelivery {
        @Test
        @DisplayName("배송 준비 -> 201 + Location + JSON 검증")
        void success() throws Exception {
            // given
            long orderId = 123L;

            given(addressMapper.toEntity(any(AddressDto.class)))
                    .willAnswer(inv -> {
                        AddressDto a = inv.getArgument(0);
                        return Address.createAddress(a.postcode(), a.state(), a.city(), a.street(), a.detail());
                    });

            AddressDto requestAddrDto = createOrderSampleAddrDto();
            DeliverySummaryResult expected =
                    new DeliverySummaryResult(DeliveryStatus.READY, null,
                            requestAddrDto, null, null);

            given(orderService.prepareDelivery(eq(orderId), any(Address.class)))
                    .willReturn(expected);

            given(deliveryApiMapper.toDeliverySummaryResponse(expected))
                    .willReturn(new DeliverySummaryResponse(DeliveryStatus.READY, null,
                            requestAddrDto, null, null));

            // when
            ResultActions result = mockMvc.perform(post("/orders/{id}/delivery", orderId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(requestAddrDto)));

            // then
            result.andExpect(status().isCreated())
                    .andExpect(header().string("Location", Matchers.endsWith("/orders/" + orderId + "/delivery")))
                    .andExpect(jsonPath("$.deliveryStatus").value("READY"))
                    .andExpect(jsonPath("$.shipAddr.city").value(requestAddrDto.city()));

            then(addressMapper).should(times(1)).toEntity(any(AddressDto.class));
            then(orderService).should(times(1)).prepareDelivery(eq(orderId), any(Address.class));
        }

        @Test
        @DisplayName("회원 주소 / 배송 주소 없음 -> 422 에러")
        void shouldFail_whenShipAddrAndMemberAddrIsNull() throws Exception {
            // given
            long orderId = 123L;

            given(orderService.prepareDelivery(eq(orderId), isNull()))
                    .willThrow(InvalidAddressException.class);

            // when
            ResultActions result = mockMvc.perform(post("/orders/{id}/delivery", orderId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("null"));

            // then
            result.andExpect(status().isUnprocessableEntity());

            then(addressMapper).shouldHaveNoInteractions();
            then(orderService).should(times(1)).prepareDelivery(eq(orderId), isNull());
        }


    }

    @Nested
    @DisplayName("PATCH /orders/{id}/delivery")
    class StartDelivery {

        StartDeliveryRequest request = new StartDeliveryRequest("12345", LocalDateTime.of(2025, 11, 12, 13, 30));

        @Test
        @DisplayName("배송 시작 -> 204 검증")
        void success() throws Exception {
            // given
            long orderId = 123L;
            willDoNothing()
                    .given(orderService)
                    .startDelivery(eq(orderId), eq(request.trackingNo()), eq(request.shippedAt()));

            // when
            ResultActions result = mockMvc.perform(patch("/orders/{id}/delivery", orderId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)));

            // then
            result.andExpect(status().isNoContent());

            then(orderService).should(times(1))
                    .startDelivery(orderId, request.trackingNo(), request.shippedAt());
        }

        @Test
        @DisplayName("배송 시작 시간 미설정 -> 204 검증")
        void success_whenShippedAtIsNull() throws Exception {
            // given
            long orderId = 123L;
            willDoNothing()
                    .given(orderService)
                    .startDelivery(eq(orderId), eq(request.trackingNo()), isNull());

            // when
            ResultActions result = mockMvc.perform(patch("/orders/{id}/delivery", orderId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)));

            // then
            result.andExpect(status().isNoContent());

            then(orderService).should(times(1))
                    .startDelivery(eq(orderId), eq(request.trackingNo()), isNotNull());
        }

        @Test
        @DisplayName("결제 되지 않은 상태 -> 422 에러")
        void shouldFail_whenNotPaid() throws Exception {
            // given
            long orderId = 123L;

            willThrow(PaymentRequiredException.class)
                    .given(orderService)
                    .startDelivery(eq(orderId), eq(request.trackingNo()), eq(request.shippedAt()));

            // when
            ResultActions result = mockMvc.perform(patch("/orders/{id}/delivery", orderId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)));

            // then
            result.andExpect(status().isUnprocessableEntity())
                    .andExpect(jsonPath("$.status").value(422));

            then(orderService).should(times(1))
                    .startDelivery(orderId, request.trackingNo(), request.shippedAt());
        }

        @Test
        @DisplayName("배송 준비되지 않은 상태 -> 422 에러")
        void shouldFail_whenNotPrepared() throws Exception {
            // given
            long orderId = 123L;

            willThrow(DeliveryException.class)
                    .given(orderService)
                    .startDelivery(eq(orderId), eq(request.trackingNo()), eq(request.shippedAt()));

            // when
            ResultActions result = mockMvc.perform(patch("/orders/{id}/delivery", orderId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)));

            // then
            result.andExpect(status().isUnprocessableEntity())
                    .andExpect(jsonPath("$.status").value(422));

            then(orderService).should(times(1))
                    .startDelivery(orderId, request.trackingNo(), request.shippedAt());
        }
    }

    @Nested
    @DisplayName("PATCH /orders/{id}/delivery/complete")
    class CompleteDelivery {

        CompleteDeliveryRequest request = new CompleteDeliveryRequest(LocalDateTime.of(2025, 11, 15, 13, 30));

        @Test
        @DisplayName("배송 완료 -> 204 검증")
        void success() throws Exception {
            // given
            long orderId = 123L;
            willDoNothing()
                    .given(orderService)
                    .completeDelivery(orderId, request.arrivedAt());

            // when
            ResultActions result = mockMvc.perform(patch("/orders/{id}/delivery/complete", orderId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)));

            // then
            result.andExpect(status().isNoContent());

            then(orderService).should(times(1))
                    .completeDelivery(orderId, request.arrivedAt());
        }

        @Test
        @DisplayName("도착 시간 미설정 -> 204 검증")
        void success_whenArrivedAtIsNull() throws Exception {
            // given
            long orderId = 123L;
            willDoNothing()
                    .given(orderService)
                    .completeDelivery(eq(orderId), isNull());

            // when
            ResultActions result = mockMvc.perform(patch("/orders/{id}/delivery/complete", orderId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)));

            // then
            result.andExpect(status().isNoContent());

            then(orderService).should(times(1))
                    .completeDelivery(eq(orderId), isNotNull());
        }

        @Test
        @DisplayName("결제 되지 않은 상태 -> 422 에러")
        void shouldFail_whenNotPaid() throws Exception {
            // given
            long orderId = 123L;

            willThrow(PaymentRequiredException.class)
                    .given(orderService)
                    .completeDelivery(orderId, request.arrivedAt());

            // when
            ResultActions result = mockMvc.perform(patch("/orders/{id}/delivery/complete", orderId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)));

            // then
            result.andExpect(status().isUnprocessableEntity())
                    .andExpect(jsonPath("$.status").value(422));

            then(orderService).should(times(1))
                    .completeDelivery(orderId, request.arrivedAt());
        }

        @Test
        @DisplayName("배송 준비되지 않은 상태 -> 422 에러")
        void shouldFail_whenNotPrepared() throws Exception {
            // given
            long orderId = 123L;

            willThrow(DeliveryException.class)
                    .given(orderService)
                    .completeDelivery(orderId, request.arrivedAt());

            // when
            ResultActions result = mockMvc.perform(patch("/orders/{id}/delivery/complete", orderId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)));

            // then
            result.andExpect(status().isUnprocessableEntity())
                    .andExpect(jsonPath("$.status").value(422));

            then(orderService).should(times(1))
                    .completeDelivery(orderId, request.arrivedAt());
        }

        @Test
        @DisplayName("배송 시작 전 -> 422 에러")
        void shouldFail_whenNotShipped() throws Exception {
            // given
            long orderId = 123L;

            willThrow(DeliveryStatusException.class)
                    .given(orderService)
                    .completeDelivery(orderId, request.arrivedAt());

            // when
            ResultActions result = mockMvc.perform(patch("/orders/{id}/delivery/complete", orderId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)));

            // then
            result.andExpect(status().isUnprocessableEntity())
                    .andExpect(jsonPath("$.status").value(422));

            then(orderService).should(times(1))
                    .completeDelivery(orderId, request.arrivedAt());
        }
    }



    private AddressDto createOrderSampleAddrDto() {
        return new AddressDto(
                "12345",
                "광주광역시",
                "광산구",
                "신창동",
                "상가 1층"
        );
    }

}
