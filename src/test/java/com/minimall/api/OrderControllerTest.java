package com.minimall.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.minimall.domain.embeddable.Address;
import com.minimall.domain.embeddable.AddressDto;
import com.minimall.domain.embeddable.AddressMapper;
import com.minimall.domain.embeddable.InvalidAddressException;
import com.minimall.domain.order.OrderStatus;
import com.minimall.domain.order.delivery.DeliveryStatus;
import com.minimall.domain.order.delivery.dto.DeliverySummaryDto;
import com.minimall.domain.order.dto.request.OrderCreateRequestDto;
import com.minimall.domain.order.dto.request.OrderItemCreateDto;
import com.minimall.domain.order.dto.response.OrderCreateResponseDto;
import com.minimall.domain.order.dto.response.OrderDetailResponseDto;
import com.minimall.domain.order.dto.response.OrderItemResponseDto;
import com.minimall.domain.order.exception.OrderStatusException;
import com.minimall.domain.order.pay.PayAmountMismatchException;
import com.minimall.domain.order.pay.PayMethod;
import com.minimall.domain.order.pay.PayStatus;
import com.minimall.domain.order.pay.dto.PayRequestDto;
import com.minimall.domain.order.pay.dto.PaySummaryDto;
import com.minimall.service.OrderService;
import com.minimall.service.exception.MemberNotFoundException;
import com.minimall.service.exception.OrderNotFoundException;
import com.minimall.service.exception.ProductNotFoundException;
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
    AddressMapper addressMapper;

    @MockitoBean
    OrderService orderService;

    private OrderCreateRequestDto createRequest;
    private OrderDetailResponseDto detailResponse;

    private static final long NOT_EXIST_ID = 999_999_999L;

    @BeforeEach
    void setUp() {
        createRequest = new OrderCreateRequestDto(
                1L,
                List.of(new OrderItemCreateDto(1L, 10),
                        new OrderItemCreateDto(2L, 20)));

        detailResponse = new OrderDetailResponseDto(
                1L,
                LocalDateTime.of(2025, 11, 11, 12, 30),
                OrderStatus.ORDERED,
                100_000,
                List.of(new OrderItemResponseDto(
                        1L,
                        "도서",
                        10_000,
                        10,
                        100_000)),
                null, null);
    }

    @Nested
    @DisplayName("POST /orders")
    class Create {
        @Test
        @DisplayName("주문 생성 -> 201 + JSON 검증")
        void return201_whenSuccess() throws Exception {
            //given
            given(orderService.createOrder(any())).willReturn(
                    new OrderCreateResponseDto(1L, LocalDateTime.now(), OrderStatus.ORDERED,
                            100_000, 0, 100_000, 2)
            );

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
    @DisplayName("GET /orders/{id}")
    class GetOrderDetail {
        @Test
        @DisplayName("주문 단건 상세 조회 -> 200 + JSON 검증")
        void return200_whenSuccess() throws Exception {
            //given
            given(orderService.getOrderDetail(1L)).willReturn(detailResponse);

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
            PayRequestDto request = new PayRequestDto(PayMethod.CARD, 100_000);

            PaySummaryDto response = new PaySummaryDto(
                    PayMethod.CARD,
                    100_000,
                    PayStatus.PAID,
                    LocalDateTime.of(2025, 11, 11, 13, 30));

            given(orderService.processPayment(eq(orderId), any(PayRequestDto.class)))
                    .willReturn(response);

            //when
            ResultActions result = mockMvc.perform(post("/orders/" + orderId + "/payment")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)));

            //then
            result.andExpect(status().isCreated())
                    .andExpect(header().string("Location", Matchers.endsWith("/orders/" + orderId + "/payment")))
                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.payAmount").value(100_000))
                    .andExpect(jsonPath("$.payStatus").value("PAID"));
        }

        @Test
        @DisplayName("중복 결제 -> 422 Unprocessable Entity")
        void shouldFail_whenDuplicatedPay() throws Exception{
            //given
            long orderId = 123L;
            PayRequestDto request = new PayRequestDto(PayMethod.CARD, 100_000);

            PaySummaryDto response = new PaySummaryDto(
                    PayMethod.CARD,
                    100_000,
                    PayStatus.PAID,
                    LocalDateTime.of(2025, 11, 11, 13, 30));

            given(orderService.processPayment(eq(orderId), any(PayRequestDto.class)))
                    .willReturn(response)
                    .willThrow(OrderStatusException.class);

            // when-then(1): 첫 결제 성공 -> 201
            mockMvc.perform(post("/orders/{id}/payment", orderId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated());

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
            PayRequestDto request = new PayRequestDto(PayMethod.CARD, 100_000);

            given(orderService.processPayment(eq(orderId), any(PayRequestDto.class)))
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

            AddressDto requestAddrDto = createSampleAddrDto();
            DeliverySummaryDto expected =
                    new DeliverySummaryDto(DeliveryStatus.READY, null,
                            requestAddrDto, null, null);

            given(orderService.prepareDelivery(eq(orderId), any(Address.class)))
                    .willReturn(expected);

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


        private AddressDto createSampleAddrDto() {
            return new AddressDto(
                    "12345",
                    "광주광역시",
                    "광산구",
                    "신창동",
                    "상가 1층"
            );
        }
    }
}
