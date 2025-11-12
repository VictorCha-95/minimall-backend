package com.minimall.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.minimall.domain.embeddable.Address;
import com.minimall.domain.embeddable.AddressDto;
import com.minimall.domain.embeddable.AddressMapper;
import com.minimall.domain.embeddable.InvalidAddressException;
import com.minimall.domain.member.Member;
import com.minimall.domain.member.MemberRepository;
import com.minimall.domain.order.OrderRepository;
import com.minimall.domain.order.delivery.DeliveryStatus;
import com.minimall.domain.order.delivery.dto.DeliverySummaryDto;
import com.minimall.domain.order.dto.request.OrderCreateRequestDto;
import com.minimall.domain.order.dto.request.OrderItemCreateDto;
import com.minimall.domain.order.dto.response.OrderCreateResponseDto;
import com.minimall.domain.order.exception.OrderStatusException;
import com.minimall.domain.order.pay.PayAmountMismatchException;
import com.minimall.domain.order.pay.PayMethod;
import com.minimall.domain.order.pay.PayStatus;
import com.minimall.domain.order.pay.dto.PayRequestDto;
import com.minimall.domain.order.pay.dto.PaySummaryDto;
import com.minimall.domain.product.Product;
import com.minimall.domain.product.ProductRepository;
import com.minimall.service.OrderService;
import com.minimall.service.exception.OrderNotFoundException;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.BDDMockito.*;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.times;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@ActiveProfiles("integration-test")
@AutoConfigureMockMvc
@Testcontainers
@Transactional
class OrderControllerIntegrationTest {

    @ServiceConnection
    static final MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.0")
            .withReuse(true);

    @Autowired
    MockMvc mockMvc;

    @Autowired
    MemberRepository memberRepository;

    @Autowired
    ProductRepository productRepository;

    @Autowired
    OrderRepository orderRepository;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    AddressMapper addressMapper;

    @Autowired
    OrderService orderService;

    Member savedMember;
    Member savedMemberAddrIsNull;

    List<OrderItemCreateDto> orderItems = new ArrayList<>();

    private OrderCreateRequestDto createRequest;
    private OrderCreateRequestDto createRequestMemberAddrIsNull;

    private static final long NOT_EXIST_ID = 999_999_999L;


    @BeforeEach
    void setUp() {
        //== Member Entity ==//
        Member member = Member.builder()
                .loginId("user1")
                .password("abc12345")
                .name("차태승")
                .email("cts9458@naver.com")
                .addr(new Address("12345", "광주광역시", "광산구", "수등로76번길 40", "123동 1501호"))
                .build();
        Member memberAddrIsNull = Member.builder()
                .loginId("user1")
                .password("abc12345")
                .name("차태승")
                .email("cts9458@naver.com")
                .addr(null)
                .build();
        savedMember = memberRepository.save(member);
        savedMemberAddrIsNull = memberRepository.save(memberAddrIsNull);

        //== Product Entity ==//
        Product book = new Product("도서", 20000, 50);
        Product keyboard = new Product("키보드", 100000, 20);
        Product savedBook = productRepository.save(book);
        Product savedKeyboard = productRepository.save(keyboard);

        //== OrderItemRequestList ==//
        OrderItemCreateDto orderItemCreateRequest1 = new OrderItemCreateDto(savedBook.getId(), 30);
        OrderItemCreateDto orderItemCreateRequest2 = new OrderItemCreateDto(savedKeyboard.getId(), 10);
        orderItems.add(orderItemCreateRequest1);
        orderItems.add(orderItemCreateRequest2);

        //== OrderCreateRequest ==//
        createRequest = new OrderCreateRequestDto(savedMember.getId(), orderItems);
        createRequestMemberAddrIsNull = new OrderCreateRequestDto(savedMemberAddrIsNull.getId(), orderItems);
    }

    @Nested
    @DisplayName("POST /orders")
    class Create {
        @Test
        @DisplayName("주문 생성 -> 201 + JSON + Location 검증")
        void return201_whenOrderCreate() throws Exception {
            //when
            ResultActions result = mockMvc.perform(post("/orders")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(createRequest)));

            //then
            MvcResult mvcResult = result.andExpect(status().isCreated())
                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.id").isNumber())
                    .andExpect(jsonPath("$.itemCount").value(createRequest.items().size()))
                    .andReturn();

            String json = mvcResult.getResponse().getContentAsString();
            OrderCreateResponseDto body = objectMapper.readValue(json, OrderCreateResponseDto.class);

            String location = mvcResult.getResponse().getHeader("Location");
            assertThat(location).endsWith("/orders/" + body.id());
        }

        @Test
        @DisplayName("회원 미존재 -> 404 Not Found")
        void return404_whenMemberNotFound() throws Exception{
            //when
            ResultActions result = mockMvc.perform(post("/orders")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(new OrderCreateRequestDto(NOT_EXIST_ID, orderItems))));

            //then
            result.andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.status").value(404))
                    .andExpect(jsonPath("$.errorCode").value("NOT_FOUND"))
                    .andExpect(jsonPath("$.path").value("/orders"))
                    .andExpect(jsonPath("$.message").exists())
                    .andExpect(header().doesNotExist("Location"));
        }

        @Test
        @DisplayName("상품 미존재 -> 404 Not Found")
        void return404_whenProductNotFound() throws Exception{
            //when
            ResultActions result = mockMvc.perform(post("/orders")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(
                            new OrderCreateRequestDto(savedMember.getId(),
                                    List.of(orderItems.getFirst(),
                                            new OrderItemCreateDto(NOT_EXIST_ID, 999))))));

            //then
            result.andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.status").value(404))
                    .andExpect(jsonPath("$.errorCode").value("NOT_FOUND"))
                    .andExpect(jsonPath("$.path").value("/orders"))
                    .andExpect(jsonPath("$.message").exists())
                    .andExpect(header().doesNotExist("Location"));
        }
    }

    @Nested
    @DisplayName("GET /orders/{id}")
    class GetOrderDetail {
        @Test
        @DisplayName("주문 단건 상세 조회 -> 200 + JSON 검증")
        void return200_whenSuccess() throws Exception {
            //given
            OrderCreateResponseDto order = orderService.createOrder(createRequest);

            //when
            ResultActions result = mockMvc.perform(get("/orders/" + order.id()));

            //then
            result.andExpect(status().isOk())
                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.id").value(order.id()));
        }

        @Test
        @DisplayName("주문 없음 -> 404 Not Found")
        void return404_whenOrderNotFound() throws Exception {
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
            OrderCreateResponseDto order = orderService.createOrder(createRequest);
            PayRequestDto request = new PayRequestDto(PayMethod.CARD, order.finalAmount());

            //when
            ResultActions result = mockMvc.perform(post("/orders/" + order.id() + "/payment")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)));

            //then
            MvcResult mvcResult = result.andExpect(status().isCreated())
                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.payAmount").value(order.finalAmount()))
                    .andExpect(jsonPath("$.payStatus").value("PAID"))
                    .andReturn();

            String json = mvcResult.getResponse().getContentAsString();
            PaySummaryDto body = objectMapper.readValue(json, PaySummaryDto.class);

            String location = mvcResult.getResponse().getHeader("Location");
            assertThat(location).endsWith("/orders/" + order.id() + "/payment");
        }

        @Test
        @DisplayName("중복 결제 -> 422 Unprocessable Entity")
        void shouldFail_whenDuplicatedPay() throws Exception{
            ///given
            OrderCreateResponseDto order = orderService.createOrder(createRequest);
            PayRequestDto request = new PayRequestDto(PayMethod.CARD, order.finalAmount());

            // when-then(1): 첫 결제 성공 -> 201
            mockMvc.perform(post("/orders/{id}/payment", order.id())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated());

            // when-then(2): 동일 요청 재시도 -> 422
            mockMvc.perform(post("/orders/{id}/payment", order.id())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isUnprocessableEntity())
                    .andExpect(jsonPath("$.status").value(422));
        }

        @Test
        @DisplayName("결제 금액 오류 -> 422 Unprocessable Entity")
        void shouldFail_whenMismatchAmount() throws Exception{
            ///given
            int invalidAmount = 999_999;
            OrderCreateResponseDto order = orderService.createOrder(createRequest);
            PayRequestDto request = new PayRequestDto(PayMethod.CARD, invalidAmount);

            // when-then
            mockMvc.perform(post("/orders/{id}/payment", order.id())
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
            long orderId = createOrderAndProcessPayment(createRequest);

            AddressDto requestAddrDto = createSampleAddrDto();

            // when
            ResultActions result = mockMvc.perform(post("/orders/{id}/delivery", orderId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(requestAddrDto)));

            // then
            result.andExpect(status().isCreated())
                    .andExpect(header().string("Location", Matchers.endsWith("/orders/" + orderId + "/delivery")))
                    .andExpect(jsonPath("$.deliveryStatus").value("READY"))
                    .andExpect(jsonPath("$.shipAddr.city").value(requestAddrDto.city()));
        }

        @Test
        @DisplayName("회원 주소 / 배송 주소 없음 -> 422 에러")
        void shouldFail_whenShipAddrAndMemberAddrIsNull() throws Exception {
            // given
            long orderId = createOrderAndProcessPayment(createRequestMemberAddrIsNull);
            Member member = memberRepository.findById(createRequestMemberAddrIsNull.memberId()).get();
            assertThat(member.getAddr()).isNull();

            // when
            ResultActions result = mockMvc.perform(post("/orders/{id}/delivery", orderId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("null"));

            // then
            result.andExpect(status().isUnprocessableEntity())
                    .andExpect(jsonPath("$.status").value(422));
        }

        private long createOrderAndProcessPayment(OrderCreateRequestDto request) {
            OrderCreateResponseDto order = orderService.createOrder(request);
            orderService.processPayment(order.id(), new PayRequestDto(PayMethod.CARD, order.finalAmount()));
            return order.id();
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