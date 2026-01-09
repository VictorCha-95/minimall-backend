package com.minimall.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.minimall.AbstractIntegrationTest;
import com.minimall.api.order.delivery.dto.StartDeliveryRequest;
import com.minimall.api.order.dto.request.CompleteDeliveryRequest;
import com.minimall.domain.embeddable.Address;
import com.minimall.api.common.embeddable.AddressDto;
import com.minimall.api.common.embeddable.AddressMapper;
import com.minimall.domain.member.Member;
import com.minimall.domain.member.MemberRepository;
import com.minimall.domain.order.OrderRepository;
import com.minimall.api.order.dto.request.OrderCreateRequest;
import com.minimall.api.order.dto.request.OrderItemCreateRequest;
import com.minimall.api.order.dto.response.OrderCreateResponse;
import com.minimall.fixture.AddressFixture;
import com.minimall.fixture.MemberFixture;
import com.minimall.fixture.OrderFixture;
import com.minimall.fixture.PayFixture;
import com.minimall.fixture.ProductFixture;
import com.minimall.service.order.dto.command.OrderCreateCommand;
import com.minimall.domain.order.pay.PayMethod;
import com.minimall.api.order.pay.dto.PayRequest;
import com.minimall.domain.product.Product;
import com.minimall.domain.product.ProductRepository;
import com.minimall.service.order.OrderService;
import com.minimall.service.order.dto.result.OrderCreateResult;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@Transactional
class OrderControllerIntegrationTest extends AbstractIntegrationTest {

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

    List<OrderItemCreateRequest> orderItems = new ArrayList<>();

    private OrderCreateRequest orderCreateRequest;
    private OrderCreateRequest createRequestMemberAddrIsNull;

    private OrderCreateCommand orderCreateCommand;
    private OrderCreateCommand createCommandMemberAddrIsNull;

    private static final long NOT_EXIST_ID = 999_999_999L;


    @BeforeEach
    void setUp() {
        //== Member Entity ==//
        Address address = AddressFixture.createAddress(
                "62550",
                "광주광역시",
                "광산구",
                "수등로76번길 40",
                "123동 456호"
        );
        Member member = MemberFixture.createMember(
                "user123",
                "차태승",
                "user123@example.com",
                address
        );

        Member memberAddrIsNull = MemberFixture.createMember(
                "abcuser123",
                "abc차태승",
                "abcuser123@example.com",
                null
        );

        savedMember = memberRepository.save(member);
        savedMemberAddrIsNull = memberRepository.save(memberAddrIsNull);

        //== Product Entity ==//
        Product savedBook = ProductFixture.createProductSaved(productRepository, "도서", 20_000, 50);
        Product savedKeyboard = ProductFixture.createProductSaved(productRepository, "키보드", 100_000, 20);

        //== OrderItemRequestList ==//
        OrderItemCreateRequest orderItemCreateRequest1 = OrderFixture.createOrderItemRequest(savedBook.getId(), 30);
        OrderItemCreateRequest orderItemCreateRequest2 = OrderFixture.createOrderItemRequest(savedKeyboard.getId(), 10);
        orderItems.add(orderItemCreateRequest1);
        orderItems.add(orderItemCreateRequest2);

        //== OrderCreateRequest ==//
        orderCreateRequest = OrderFixture.createOrderRequestDto(savedMember.getId(), orderItems);
        createRequestMemberAddrIsNull = OrderFixture.createOrderRequestDto(savedMemberAddrIsNull.getId(), orderItems);

        orderCreateCommand = OrderFixture.createOrderCreateCommand(
                orderCreateRequest.memberId(),
                List.of(
                        OrderFixture.createOrderItemCommand(savedBook.getId(), 30),
                        OrderFixture.createOrderItemCommand(savedKeyboard.getId(), 10)
                )
        );

        createCommandMemberAddrIsNull = OrderFixture.createOrderCreateCommand(
                createRequestMemberAddrIsNull.memberId(),
                List.of(
                        OrderFixture.createOrderItemCommand(savedBook.getId(), 30),
                        OrderFixture.createOrderItemCommand(savedKeyboard.getId(), 10)
                )
        );
    }

    @Nested
    @DisplayName("POST /api/orders")
    class CreateOrder {
        @Test
        @DisplayName("주문 생성 -> 201 + JSON + Location 검증")
        void return201_whenOrderCreate() throws Exception {
            //when
            ResultActions result = mockMvc.perform(post("/api/orders")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(orderCreateRequest)));

            //then
            MvcResult mvcResult = result.andExpect(status().isCreated())
                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.id").isNumber())
                    .andExpect(jsonPath("$.itemCount").value(orderCreateRequest.items().size()))
                    .andReturn();

            String json = mvcResult.getResponse().getContentAsString();
            OrderCreateResponse body = objectMapper.readValue(json, OrderCreateResponse.class);

            String location = mvcResult.getResponse().getHeader("Location");
            assertThat(location).endsWith("/api/orders/" + body.id());
        }

        @Test
        @DisplayName("회원 미존재 -> 404 Not Found")
        void return404_whenMemberNotFound() throws Exception{
            //when
            ResultActions result = mockMvc.perform(post("/api/orders")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(
                            OrderFixture.createOrderRequestDto(NOT_EXIST_ID, orderItems)
                    )));

            //then
            result.andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.status").value(404))
                    .andExpect(jsonPath("$.errorCode").value("NOT_FOUND"))
                    .andExpect(jsonPath("$.path").value("/api/orders"))
                    .andExpect(jsonPath("$.message").exists())
                    .andExpect(header().doesNotExist("Location"));
        }

        @Test
        @DisplayName("상품 미존재 -> 404 Not Found")
        void return404_whenProductNotFound() throws Exception{
            //when
            ResultActions result = mockMvc.perform(post("/api/orders")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(
                            OrderFixture.createOrderRequestDto(savedMember.getId(),
                                    List.of(orderItems.getFirst(),
                                            OrderFixture.createOrderItemRequest(NOT_EXIST_ID, 999))))));

            //then
            result.andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.status").value(404))
                    .andExpect(jsonPath("$.errorCode").value("NOT_FOUND"))
                    .andExpect(jsonPath("$.path").value("/api/orders"))
                    .andExpect(jsonPath("$.message").exists())
                    .andExpect(header().doesNotExist("Location"));
        }
    }

    @Nested
    @DisplayName("PATCH /api/orders/{id}/cancel")
    class CancelOrder {
        @DisplayName("주문 취소 -> 204 검증")
        @Test
        void return204_whenOrderCancel() throws Exception {
            //given
            OrderCreateResult order = orderService.createOrder(orderCreateCommand);

            //when
            ResultActions result = mockMvc.perform(patch("/api/orders/" + order.id() + "/cancel"));

            //then
            result.andExpect(status().isNoContent());
        }

        @DisplayName("주문 미존재 -> 404 NotFound 예외 발생")
        @Test
        void return404_whenOrderNotFound() throws Exception {
            //when
            ResultActions result = mockMvc.perform(patch("/api/orders/" + NOT_EXIST_ID + "/cancel"));

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
    @DisplayName("GET /api/orders/{id}")
    class GetOrderDetail {
        @Test
        @DisplayName("주문 단건 상세 조회 -> 200 + JSON 검증")
        void return200_whenSuccess() throws Exception {
            //given
            OrderCreateResult order = orderService.createOrder(orderCreateCommand);
            Long id = order.id();

            //when
            ResultActions result = mockMvc.perform(get("/api/orders/" + id));

            //then
            result.andExpect(status().isOk())
                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.id").value(id));
        }

        @Test
        @DisplayName("주문 없음 -> 404 Not Found")
        void return404_whenOrderNotFound() throws Exception {
            //when
            ResultActions result = mockMvc.perform(get("/api/orders/" + NOT_EXIST_ID));

            //then
            result.andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.status").value(404))
                    .andExpect(jsonPath("$.errorCode").value("NOT_FOUND"))
                    .andExpect(jsonPath("$.path").value("/api/orders/" + NOT_EXIST_ID))
                    .andExpect(jsonPath("$.message", Matchers.containsString("주문")))
                    .andExpect(jsonPath("$.message", Matchers.containsString("id")))
                    .andExpect(jsonPath("$.message", Matchers.containsString(String.valueOf(NOT_EXIST_ID))))
                    .andExpect(jsonPath("$.timestamp").exists());
        }
    }

    @Nested
    @DisplayName("POST /api/orders/{id}/payment")
    class ProcessPayment {
        @Test
        @DisplayName("주문 결제 처리 -> 201 + Location 헤더 + JSON 검증")
        void success() throws Exception{
            //given
            OrderCreateResult order = orderService.createOrder(orderCreateCommand);
            Long id = order.id();
            PayRequest request = PayFixture.createPayRequest(PayMethod.CARD, order.finalAmount());

            //when
            ResultActions result = mockMvc.perform(post("/api/orders/" + id + "/payment")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)));

            //then
            MvcResult mvcResult = result.andExpect(status().isOk())
                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.payAmount").value(order.finalAmount()))
                    .andExpect(jsonPath("$.payStatus").value("PAID"))
                    .andReturn();
        }

        @Test
        @DisplayName("중복 결제 -> 422 Unprocessable Entity")
        void shouldFail_whenDuplicatedPay() throws Exception{
            ///given
            OrderCreateResult order = orderService.createOrder(orderCreateCommand);
            Long id = order.id();
            PayRequest request = PayFixture.createPayRequest(PayMethod.CARD, order.finalAmount());

            // when-then(1): 첫 결제 성공 -> 201
            mockMvc.perform(post("/api/orders/{id}/payment", id)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk());

            // when-then(2): 동일 요청 재시도 -> 422
            mockMvc.perform(post("/api/orders/{id}/payment", id)
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
            OrderCreateResult order = orderService.createOrder(orderCreateCommand);
            Long id = order.id();
            PayRequest request = PayFixture.createPayRequest(PayMethod.CARD, invalidAmount);

            // when-then
            mockMvc.perform(post("/api/orders/{id}/payment", id)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isUnprocessableEntity())
                    .andExpect(jsonPath("$.status").value(422));
        }
    }

    @Nested
    @DisplayName("POST /api/orders/{id}/delivery")
    class PrepareDelivery {
        private AddressDto createSampleAddrDto() {
            return AddressFixture.createAddressDto(
                    "12345",
                    "광주광역시",
                    "광산구",
                    "신창동",
                    "상가 1층"
            );
        }
        @Test
        @DisplayName("배송 준비 -> 201 + Location + JSON 검증")
        void success() throws Exception {
            // given
            long orderId = createOrderAndProcessPayment(orderCreateCommand);

            AddressDto requestAddrDto = createSampleAddrDto();

            // when
            ResultActions result = mockMvc.perform(post("/api/orders/{id}/delivery", orderId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(requestAddrDto)));

            // then
            result.andExpect(status().isCreated())
                    .andExpect(header().string("Location", Matchers.endsWith("/api/orders/" + orderId + "/delivery")))
                    .andExpect(jsonPath("$.deliveryStatus").value("READY"))
                    .andExpect(jsonPath("$.shipAddr.city").value(requestAddrDto.city()));
        }

        @Test
        @DisplayName("회원 주소 / 배송 주소 없음 -> 422 에러")
        void shouldFail_whenShipAddrAndMemberAddrIsNull() throws Exception {
            // given
            long orderId = createOrderAndProcessPayment(createCommandMemberAddrIsNull);
            Member member = memberRepository.findById(createRequestMemberAddrIsNull.memberId()).get();
            assertThat(member.getAddr()).isNull();

            // when
            ResultActions result = mockMvc.perform(post("/api/orders/{id}/delivery", orderId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("null"));

            // then
            result.andExpect(status().isUnprocessableEntity())
                    .andExpect(jsonPath("$.status").value(422));
        }

        private long createOrderAndProcessPayment(OrderCreateCommand command) {
            OrderCreateResult order = orderService.createOrder(command);
            Long id = order.id();
            orderService.processPayment(
                    id,
                    PayFixture.createPayCommand(PayMethod.CARD, order.finalAmount())
            );
            return id;
        }


    }

    @Nested
    @DisplayName("PATCH /api/orders/{id}/delivery (Long, StartDeliveryRequest)")
    class StartDelivery {

        StartDeliveryRequest request = OrderFixture.createStartDeliveryRequest(
                "12345",
                LocalDateTime.of(2025, 11, 12, 13, 30)
        );
        Address shipAddr = AddressFixture.createAddress(
                "12345",
                "광주광역시",
                "광산구",
                "신창동",
                "상가 1층"
        );

        private Long prepareDelivery() {
            Long orderId = processPayment();
            orderService.prepareDelivery(orderId, shipAddr);
            return orderId;
        }

        private Long processPayment() {
            OrderCreateResult order = orderService.createOrder(orderCreateCommand);
            Long orderId = order.id();
            orderService.processPayment(
                    orderId,
                    PayFixture.createPayCommand(PayMethod.MOBILE_PAY, order.finalAmount()));
            return orderId;
        }

        @Test
        @DisplayName("배송 시작 -> 204 검증")
        void success() throws Exception {
            // given
            Long orderId = prepareDelivery();

            // when
            ResultActions result = mockMvc.perform(patch("/api/orders/{id}/delivery", orderId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)));

            // then
            result.andExpect(status().isNoContent());
        }


        @Test
        @DisplayName("결제 되지 않은 상태 -> 422 에러")
        void shouldFail_whenNotPaid() throws Exception {
            // given
            OrderCreateResult order = orderService.createOrder(orderCreateCommand);
            Long orderId = order.id();

            // when
            ResultActions result = mockMvc.perform(patch("/api/orders/{id}/delivery", orderId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)));

            // then
            result.andExpect(status().isUnprocessableEntity())
                    .andExpect(jsonPath("$.status").value(422));
        }

        @Test
        @DisplayName("배송 준비되지 않은 상태 -> 422 에러")
        void shouldFail_whenNotPrepared() throws Exception {
            // given
            Long orderId = processPayment();

            // when
            ResultActions result = mockMvc.perform(patch("/api/orders/{id}/delivery", orderId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)));

            // then
            result.andExpect(status().isUnprocessableEntity())
                    .andExpect(jsonPath("$.status").value(422));
        }
    }

    @Nested
    @DisplayName("PATCH /api/orders/{id}/delivery/complete")
    class CompleteDelivery {

        Address shipAddr = AddressFixture.createAddress(
                "12345",
                "광주광역시",
                "광산구",
                "신창동",
                "상가 1층"
        );

        private Long startDelivery() {
            Long orderId = prepareDelivery();
            orderService.startDelivery(orderId, "12345", LocalDateTime.of(2025, 11, 13, 13, 30));
            return orderId;
        }

        private Long prepareDelivery() {
            Long orderId = processPayment();
            orderService.prepareDelivery(orderId, shipAddr);
            return orderId;
        }

        private Long processPayment() {
            OrderCreateResult order = orderService.createOrder(orderCreateCommand);
            Long orderId = order.id();
            orderService.processPayment(
                    orderId,
                    PayFixture.createPayCommand(PayMethod.MOBILE_PAY, order.finalAmount())
            );
            return orderId;
        }

        CompleteDeliveryRequest request = OrderFixture.createCompleteDeliveryRequest(
                LocalDateTime.of(2025, 11, 15, 13, 30)
        );

        @Test
        @DisplayName("배송 완료 -> 204 검증")
        void success() throws Exception {
            // given
            Long orderId = startDelivery();

            // when
            ResultActions result = mockMvc.perform(patch("/api/orders/{id}/delivery/complete", orderId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)));

            // then
            result.andExpect(status().isNoContent());
        }

        @Test
        @DisplayName("도착 시간 미설정 -> 204 검증")
        void success_whenArrivedAtIsNull() throws Exception {
            // given
            Long orderId = startDelivery();

            // when
            ResultActions result = mockMvc.perform(patch("/api/orders/{id}/delivery/complete", orderId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(OrderFixture.createCompleteDeliveryRequest(null))));

            // then
            result.andExpect(status().isNoContent());
        }

        @Test
        @DisplayName("결제 되지 않은 상태 -> 422 에러")
        void shouldFail_whenNotPaid() throws Exception {
            // given
            OrderCreateResult order = orderService.createOrder(orderCreateCommand);
            Long orderId = order.id();

            // when
            ResultActions result = mockMvc.perform(patch("/api/orders/{id}/delivery/complete", orderId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)));

            // then
            result.andExpect(status().isUnprocessableEntity())
                    .andExpect(jsonPath("$.status").value(422));
        }

        @Test
        @DisplayName("배송 준비되지 않은 상태 -> 422 에러")
        void shouldFail_whenNotPrepared() throws Exception {
            // given
            Long orderId = processPayment();

            // when
            ResultActions result = mockMvc.perform(patch("/api/orders/{id}/delivery/complete", orderId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)));

            // then
            result.andExpect(status().isUnprocessableEntity())
                    .andExpect(jsonPath("$.status").value(422));
        }

        @Test
        @DisplayName("배송 시작 전 -> 422 에러")
        void shouldFail_whenNotShipped() throws Exception {
            // given
            Long orderId = prepareDelivery();

            // when
            ResultActions result = mockMvc.perform(patch("/api/orders/{id}/delivery/complete", orderId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)));

            // then
            result.andExpect(status().isUnprocessableEntity())
                    .andExpect(jsonPath("$.status").value(422));
        }
    }

}
