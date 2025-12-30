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
import com.minimall.domain.order.Order;
import com.minimall.domain.order.OrderRepository;
import com.minimall.api.order.dto.request.OrderCreateRequest;
import com.minimall.api.order.dto.request.OrderItemCreateRequest;
import com.minimall.api.order.dto.response.OrderCreateResponse;
import com.minimall.service.exception.OrderNotFoundException;
import com.minimall.service.order.dto.command.OrderCreateCommand;
import com.minimall.domain.order.pay.PayMethod;
import com.minimall.api.order.pay.dto.PayRequest;
import com.minimall.domain.product.Product;
import com.minimall.domain.product.ProductRepository;
import com.minimall.service.order.OrderService;
import com.minimall.service.order.dto.command.OrderItemCreateCommand;
import com.minimall.service.order.dto.command.PayCommand;
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
import static org.mockito.BDDMockito.willThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
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

    private static final String DEFAULT_LOGIN_ID = "user123";
    private static final String DEFAULT_PASSWORD_HASH = "12345678";
    private static final String DEFAULT_NAME = "차태승";
    private static final String DEFAULT_EMAIL = "user123@example.com";
    private static final Address DEFAULT_ADDRESS =
            Address.createAddress("62550", "광주광역시", "광산구", "수등로76번길 40", "123동 456호");


    @BeforeEach
    void setUp() {
        //== Member Entity ==//
        Member member = Member.registerCustomer(
                DEFAULT_LOGIN_ID, DEFAULT_PASSWORD_HASH, DEFAULT_NAME, DEFAULT_EMAIL, DEFAULT_ADDRESS
        );

        Member memberAddrIsNull = Member.registerCustomer(
                "abc" + DEFAULT_LOGIN_ID, DEFAULT_PASSWORD_HASH, "abc" + DEFAULT_NAME, "abc" + DEFAULT_EMAIL, DEFAULT_ADDRESS
        );

        savedMember = memberRepository.save(member);
        savedMemberAddrIsNull = memberRepository.save(memberAddrIsNull);

        //== Product Entity ==//
        Product book = new Product("도서", 20000, 50);
        Product keyboard = new Product("키보드", 100000, 20);
        Product savedBook = productRepository.save(book);
        Product savedKeyboard = productRepository.save(keyboard);

        //== OrderItemRequestList ==//
        OrderItemCreateRequest orderItemCreateRequest1 = new OrderItemCreateRequest(savedBook.getId(), 30);
        OrderItemCreateRequest orderItemCreateRequest2 = new OrderItemCreateRequest(savedKeyboard.getId(), 10);
        orderItems.add(orderItemCreateRequest1);
        orderItems.add(orderItemCreateRequest2);

        //== OrderCreateRequest ==//
        orderCreateRequest = new OrderCreateRequest(savedMember.getId(), orderItems);
        createRequestMemberAddrIsNull = new OrderCreateRequest(savedMemberAddrIsNull.getId(), orderItems);

        orderCreateCommand = new OrderCreateCommand(
                orderCreateRequest.memberId(),
                orderCreateRequest.items().stream()
                        .map(i -> new OrderItemCreateCommand(i.productId(), i.quantity()))
                        .toList()
        );

         createCommandMemberAddrIsNull = new OrderCreateCommand(
                createRequestMemberAddrIsNull.memberId(),
                 createRequestMemberAddrIsNull.items().stream()
                        .map(i -> new OrderItemCreateCommand(i.productId(), i.quantity()))
                        .toList()
        );
    }

    @Nested
    @DisplayName("POST /orders")
    class CreateOrder {
        @Test
        @DisplayName("주문 생성 -> 201 + JSON + Location 검증")
        void return201_whenOrderCreate() throws Exception {
            //when
            ResultActions result = mockMvc.perform(post("/orders")
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
            assertThat(location).endsWith("/orders/" + body.id());
        }

        @Test
        @DisplayName("회원 미존재 -> 404 Not Found")
        void return404_whenMemberNotFound() throws Exception{
            //when
            ResultActions result = mockMvc.perform(post("/orders")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(new OrderCreateRequest(NOT_EXIST_ID, orderItems))));

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
                            new OrderCreateRequest(savedMember.getId(),
                                    List.of(orderItems.getFirst(),
                                            new OrderItemCreateRequest(NOT_EXIST_ID, 999))))));

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
    @DisplayName("PATCH /orders/{id}/cancel")
    class CancelOrder {
        @DisplayName("주문 취소 -> 204 검증")
        @Test
        void return204_whenOrderCancel() throws Exception {
            //given
            Order order = orderService.createOrder(orderCreateCommand);

            //when
            ResultActions result = mockMvc.perform(patch("/orders/" + order.getId() + "/cancel"));

            //then
            result.andExpect(status().isNoContent());
        }

        @DisplayName("주문 미존재 -> 404 NotFound 예외 발생")
        @Test
        void return404_whenOrderNotFound() throws Exception {
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
            Order order = orderService.createOrder(orderCreateCommand);
            Long id = order.getId();

            //when
            ResultActions result = mockMvc.perform(get("/orders/" + id));

            //then
            result.andExpect(status().isOk())
                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.id").value(id));
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
            Order order = orderService.createOrder(orderCreateCommand);
            Long id = order.getId();
            PayRequest request = new PayRequest(PayMethod.CARD, order.getOrderAmount().getFinalAmount());

            //when
            ResultActions result = mockMvc.perform(post("/orders/" + id + "/payment")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)));

            //then
            MvcResult mvcResult = result.andExpect(status().isOk())
                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.payAmount").value(order.getOrderAmount().getFinalAmount()))
                    .andExpect(jsonPath("$.payStatus").value("PAID"))
                    .andReturn();
        }

        @Test
        @DisplayName("중복 결제 -> 422 Unprocessable Entity")
        void shouldFail_whenDuplicatedPay() throws Exception{
            ///given
            Order order = orderService.createOrder(orderCreateCommand);
            Long id = order.getId();
            PayRequest request = new PayRequest(PayMethod.CARD, order.getOrderAmount().getFinalAmount());

            // when-then(1): 첫 결제 성공 -> 201
            mockMvc.perform(post("/orders/{id}/payment", id)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk());

            // when-then(2): 동일 요청 재시도 -> 422
            mockMvc.perform(post("/orders/{id}/payment", id)
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
            Order order = orderService.createOrder(orderCreateCommand);
            Long id = order.getId();
            PayRequest request = new PayRequest(PayMethod.CARD, invalidAmount);

            // when-then
            mockMvc.perform(post("/orders/{id}/payment", id)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isUnprocessableEntity())
                    .andExpect(jsonPath("$.status").value(422));
        }
    }

    @Nested
    @DisplayName("POST /orders/{id}/delivery")
    class PrepareDelivery {
        private AddressDto createSampleAddrDto() {
            return new AddressDto(
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
            long orderId = createOrderAndProcessPayment(createCommandMemberAddrIsNull);
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

        private long createOrderAndProcessPayment(OrderCreateCommand command) {
            Order order = orderService.createOrder(command);
            Long id = order.getId();
            orderService.processPayment(id, new PayCommand(PayMethod.CARD, order.getOrderAmount().getFinalAmount()));
            return id;
        }


    }

    @Nested
    @DisplayName("PATCH /orders/{id}/delivery (Long, StartDeliveryRequest)")
    class StartDelivery {

        StartDeliveryRequest request = new StartDeliveryRequest("12345", LocalDateTime.of(2025, 11, 12, 13, 30));
        Address shipAddr = new Address(
                "12345",
                "광주광역시",
                "광산구",
                "신창동",
                "상가 1층");

        private Long prepareDelivery() {
            Long orderId = processPayment();
            orderService.prepareDelivery(orderId, shipAddr);
            return orderId;
        }

        private Long processPayment() {
            Order order = orderService.createOrder(orderCreateCommand);
            Long orderId = order.getId();
            orderService.processPayment(
                    orderId,
                    new PayCommand(PayMethod.MOBILE_PAY, order.getOrderAmount().getFinalAmount()));
            return orderId;
        }

        @Test
        @DisplayName("배송 시작 -> 204 검증")
        void success() throws Exception {
            // given
            Long orderId = prepareDelivery();

            // when
            ResultActions result = mockMvc.perform(patch("/orders/{id}/delivery", orderId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)));

            // then
            result.andExpect(status().isNoContent());
        }


        @Test
        @DisplayName("결제 되지 않은 상태 -> 422 에러")
        void shouldFail_whenNotPaid() throws Exception {
            // given
            Order order = orderService.createOrder(orderCreateCommand);
            Long orderId = order.getId();

            // when
            ResultActions result = mockMvc.perform(patch("/orders/{id}/delivery", orderId)
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
            ResultActions result = mockMvc.perform(patch("/orders/{id}/delivery", orderId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)));

            // then
            result.andExpect(status().isUnprocessableEntity())
                    .andExpect(jsonPath("$.status").value(422));
        }
    }

    @Nested
    @DisplayName("PATCH /orders/{id}/delivery/complete")
    class CompleteDelivery {

        Address shipAddr = new Address(
                "12345",
                "광주광역시",
                "광산구",
                "신창동",
                "상가 1층");

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
            Order order = orderService.createOrder(orderCreateCommand);
            Long orderId = order.getId();
            orderService.processPayment(orderId, new PayCommand(PayMethod.MOBILE_PAY, order.getOrderAmount().getFinalAmount()));
            return orderId;
        }

        CompleteDeliveryRequest request = new CompleteDeliveryRequest(LocalDateTime.of(2025, 11, 15, 13, 30));

        @Test
        @DisplayName("배송 완료 -> 204 검증")
        void success() throws Exception {
            // given
            Long orderId = startDelivery();

            // when
            ResultActions result = mockMvc.perform(patch("/orders/{id}/delivery/complete", orderId)
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
            ResultActions result = mockMvc.perform(patch("/orders/{id}/delivery/complete", orderId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(new CompleteDeliveryRequest(null))));

            // then
            result.andExpect(status().isNoContent());
        }

        @Test
        @DisplayName("결제 되지 않은 상태 -> 422 에러")
        void shouldFail_whenNotPaid() throws Exception {
            // given
            Order order = orderService.createOrder(orderCreateCommand);
            Long orderId = order.getId();

            // when
            ResultActions result = mockMvc.perform(patch("/orders/{id}/delivery/complete", orderId)
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
            ResultActions result = mockMvc.perform(patch("/orders/{id}/delivery/complete", orderId)
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
            ResultActions result = mockMvc.perform(patch("/orders/{id}/delivery/complete", orderId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)));

            // then
            result.andExpect(status().isUnprocessableEntity())
                    .andExpect(jsonPath("$.status").value(422));
        }
    }

}