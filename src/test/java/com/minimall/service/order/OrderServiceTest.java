package com.minimall.service.order;

import com.minimall.domain.common.DomainType;
import com.minimall.domain.embeddable.Address;
import com.minimall.api.common.embeddable.AddressDto;
import com.minimall.api.common.embeddable.AddressMapper;
import com.minimall.domain.embeddable.InvalidAddressException;
import com.minimall.domain.member.Member;
import com.minimall.domain.member.MemberRepository;
import com.minimall.domain.order.*;
import com.minimall.domain.order.delivery.DeliveryException;
import com.minimall.domain.order.delivery.DeliveryStatus;
import com.minimall.api.order.dto.request.OrderCreateRequest;
import com.minimall.api.order.dto.request.OrderItemCreateRequest;
import com.minimall.domain.order.delivery.DeliveryStatusException;
import com.minimall.domain.order.exception.OrderStatusException;
import com.minimall.domain.order.pay.PayAmountMismatchException;
import com.minimall.domain.order.pay.PayMethod;
import com.minimall.domain.product.Product;
import com.minimall.domain.product.ProductRepository;
import com.minimall.service.exception.MemberNotFoundException;
import com.minimall.service.exception.OrderNotFoundException;
import com.minimall.service.exception.ProductNotFoundException;
import com.minimall.service.order.dto.command.OrderCreateCommand;
import com.minimall.service.order.dto.command.OrderItemCreateCommand;
import com.minimall.service.order.dto.command.PayCommand;
import com.minimall.service.order.dto.mapper.DeliveryServiceMapper;
import com.minimall.service.order.dto.mapper.OrderServiceMapper;
import com.minimall.service.order.dto.mapper.PayServiceMapper;
import com.minimall.service.order.dto.result.DeliverySummaryResult;
import com.minimall.service.order.dto.result.OrderDetailResult;
import com.minimall.service.order.dto.result.OrderSummaryResult;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.assertj.core.api.SoftAssertions.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    OrderRepository orderRepository;

    @Mock
    MemberRepository memberRepository;

    @Mock
    ProductRepository productRepository;

    @Mock
    OrderServiceMapper orderServiceMapper;

    @Mock
    PayServiceMapper payMapper;

    @Mock
    DeliveryServiceMapper deliveryServiceMapper;

    @Mock
    AddressMapper addressMapper;

    @InjectMocks
    OrderService orderService;

    private OrderCreateRequest orderCreateRequest;

    private OrderCreateCommand orderCreateCommand;

    private Member member;
    private Product book;
    private Product keyboard;

    private final Long MEMBER_ID = 100L;
    private final Long PRODUCT1_ID = 1L;
    private final Long PRODUCT2_ID = 2L;



    @BeforeEach
    void setUp() {
        //== OrderItemRequestList ==//
        List<OrderItemCreateRequest> orderItems = new ArrayList<>();

        OrderItemCreateRequest orderItemCreateRequest1 = new OrderItemCreateRequest(PRODUCT1_ID, 30);
        OrderItemCreateRequest orderItemCreateRequest2 = new OrderItemCreateRequest(PRODUCT2_ID, 10);
        orderItems.add(orderItemCreateRequest1);
        orderItems.add(orderItemCreateRequest2);

        //== OrderCreateRequest ==//
        orderCreateRequest = new OrderCreateRequest(MEMBER_ID, orderItems);


        //== Member Entity ==//
        member = Member.builder()
                .loginId("user1")
                .password("abc12345")
                .name("차태승")
                .email("cts9458@naver.com")
                .addr(new Address("12345", "광주광역시", "광산구", "수등로76번길 40", "123동 1501호"))
                .build();

        //== Product Entity ==//
        book = new Product("도서", 20000, 50);
        keyboard = new Product("키보드", 100000, 20);

        orderCreateCommand = new OrderCreateCommand(
                orderCreateRequest.memberId(),
                orderCreateRequest.items().stream()
                        .map(i -> new OrderItemCreateCommand(i.productId(), i.quantity()))
                        .toList()
        );
    }

    @Nested
    @DisplayName("createOrder(OrderCreateCommand)")
    class CreateOrder {
        @Test
        @DisplayName("주문 생성 - 주문 기본 정보 및 연관관계 검증")
        void success() {
            //given
            given(memberRepository.findById(MEMBER_ID)).willReturn(Optional.of(member));
            given(productRepository.findById(PRODUCT1_ID)).willReturn(Optional.of(book));
            given(productRepository.findById(PRODUCT2_ID)).willReturn(Optional.of(keyboard));
            given(orderRepository.save(any(Order.class))).willAnswer(invocation -> invocation.getArgument(0));

            //when
            orderService.createOrder(orderCreateCommand);

            //then: 호출검증
            then(memberRepository).should(times(1)).findById(MEMBER_ID);
            then(productRepository).should(times(2)).findById(anyLong());
            then(orderRepository).should(times(1)).save(any(Order.class));
            verifyNoMoreInteractions(memberRepository, productRepository, orderRepository);

            //then: 저장된 Order 캡처
            ArgumentCaptor<Order> captor = ArgumentCaptor.forClass(Order.class);
            then(orderRepository).should().save(captor.capture());
            Order savedOrder = captor.getValue();

            //then: 저장된 Order 검증
            assertSoftly(softly ->{
                softly.assertThat(savedOrder.getOrderStatus()).isEqualTo(OrderStatus.ORDERED);
                softly.assertThat(savedOrder.getOrderedAt()).isNotNull();
                softly.assertThat(savedOrder.getMember()).isEqualTo(member);
                softly.assertThat(savedOrder.getOrderItems()).hasSize(2);
                softly.assertThat(savedOrder.getOrderAmount()).isNotNull();
                softly.assertThat(savedOrder.getPay()).isNull();
                softly.assertThat(savedOrder.getDelivery()).isNull();
            });
        }

        @Test
        @DisplayName("회원 미존재 -> 예외")
        void shouldFail_whenMemberIsNull() {
            //given
            given(memberRepository.findById(MEMBER_ID)).willReturn(Optional.empty());

            //when & then: 예외
            assertThatThrownBy(() -> orderService.createOrder(orderCreateCommand))
                    .isInstanceOfSatisfying(MemberNotFoundException.class, e -> {
                        assertThat(e.getMessage()).contains("id", orderCreateRequest.memberId().toString());
                    });

            //then: 호출 검증
            then(memberRepository).should(times(1)).findById(MEMBER_ID);
            then(productRepository).shouldHaveNoInteractions();
            then(orderRepository).shouldHaveNoInteractions();
        }

        @Test
        @DisplayName("상품 미존재 -> 예외")
        void shouldFail_whenProductIsNull() {
            //given
            given(memberRepository.findById(MEMBER_ID)).willReturn(Optional.of(member));
            given(productRepository.findById(PRODUCT1_ID)).willReturn(Optional.of(book));
            given(productRepository.findById(PRODUCT2_ID)).willReturn(Optional.empty());

            //when & then: 예외
            assertThatThrownBy(() -> orderService.createOrder(orderCreateCommand))
                    .isInstanceOfSatisfying(ProductNotFoundException.class, e -> {
                        assertThat(e.getMessage()).contains("id", PRODUCT2_ID.toString());
                    });

            //then: 호출 검증
            then(memberRepository).should(times(1)).findById(MEMBER_ID);

            then(productRepository).should(times(2)).findById(anyLong());

            then(orderRepository).shouldHaveNoInteractions();
        }
    }

    @Nested
    @DisplayName("cancelOrder(Long)")
    class CancelOrder{
        @DisplayName("주문 취소: 주문 조회 -> 취소")
        @Test
        void success(){
            //given
            Order order = mock(Order.class);
            given(orderRepository.findById(anyLong())).willReturn(Optional.of(order));

            //when
            orderService.cancelOrder(12345L);

            //then
            then(orderRepository).should(times(1)).findById(anyLong());
        }

        @DisplayName("주문 미존재: Not Found 예외 발생")
        @Test
        void shouldFail_whenOrderNotFound() {
            //given
            given(orderRepository.findById(anyLong())).willReturn(Optional.empty());

            //when - then
            assertThatThrownBy(() -> orderService.cancelOrder(12345L))
                    .isInstanceOfSatisfying(OrderNotFoundException.class, e -> {
                        assertThat(e.getMessage()).contains("id");
                    });
            then(orderRepository).should(times(1)).findById(anyLong());
        }
    }

    @Nested
    @DisplayName("getOrderDetail(Long)")
    class GetOrderDetail{
        @Test
        @DisplayName("주문 상세 단건 조회: 리포지토리 조회 -> 매버 변환 -> dto 반환")
        void success() {
            //given
            Long orderId = 1L;

            Order order = mock(Order.class);

            LocalDateTime localDateTime = LocalDateTime.of(2025, 11, 10, 15, 25, 0);
            OrderDetailResult dto = new OrderDetailResult(orderId,
                    localDateTime,
                    OrderStatus.ORDERED,
                    1_100_000,
                    List.of(), null, null);

            given(orderRepository.findById(orderId)).willReturn(Optional.of(order));
            given(orderServiceMapper.toDetailResult(order)).willReturn(dto);

            //when
            OrderDetailResult result = orderService.getOrderDetail(orderId);

            //then
            assertThat(result).isEqualTo(dto);
            then(orderRepository).should(times(1)).findById(orderId);
            then(orderServiceMapper).should(times(1)).toDetailResult(order);
            verifyNoInteractions(memberRepository, productRepository);
        }

        @Test
        @DisplayName("주문 없음: OrderNotFoundException")
        void shouldFail_whenOrderNotFound() {
            //given
            Long invalidId = 999L;

            given(orderRepository.findById(invalidId)).willReturn(Optional.empty());

            //then
            assertThatThrownBy(() -> orderService.getOrderDetail(invalidId))
                    .isInstanceOfSatisfying(OrderNotFoundException.class, e ->
                        assertThat(e.getMessage()).contains("id", String.valueOf(invalidId), DomainType.ORDER.getDisPlayName())
                    );

            then(orderRepository).should(times(1)).findById(invalidId);
            then(orderServiceMapper).shouldHaveNoInteractions();
        }
    }

    @Nested
    @DisplayName("getOrderSummaries(Long)")
    class GetOrderSummaries {
        @Test
        @DisplayName("주문 목록 요약 조회: 회원 리포지토리 조회 -> 주문 리포지토리 조회 -> 매퍼 dto 변환")
        void success() {
            //given
            Order order1 = mock(Order.class);
            Order order2 = mock(Order.class);
            List<Order> orders = List.of(order1, order2);

            Long orderId = 1L;
            LocalDateTime localDateTime = LocalDateTime.of(2025, 11, 10, 15, 25, 0);

            List<OrderSummaryResult> dtoList = List.of(new OrderSummaryResult(
                    orderId,
                    localDateTime,
                    OrderStatus.ORDERED,
                    2,
                    1_100_000), new OrderSummaryResult(
                    orderId,
                    localDateTime,
                    OrderStatus.ORDERED,
                    5,
                    5_500_000
            ));

            given(memberRepository.findById(MEMBER_ID)).willReturn(Optional.of(member));
            given(orderRepository.findByMember(member)).willReturn(orders);
            given(orderServiceMapper.toSummaryResultList(orders)).willReturn(dtoList);

            //when
            List<OrderSummaryResult> result = orderService.getOrderSummaries(MEMBER_ID);

            //then
            assertThat(result).isEqualTo(dtoList);
            then(memberRepository).should(times(1)).findById(MEMBER_ID);
            then(orderRepository).should(times(1)).findByMember(member);
            then(orderServiceMapper).should(times(1)).toSummaryResultList(orders);
            verifyNoInteractions(productRepository);
        }

        @Test
        @DisplayName("회원 주문 없음: 빈 리스트 반환")
        void returnEmpty_whenOrderIsEmpty() {
            //given
            List<Order> emptyOrder = List.of();

            List<OrderSummaryResult> dtoEmptyList = List.of();

            given(memberRepository.findById(MEMBER_ID)).willReturn(Optional.of(member));
            given(orderRepository.findByMember(member)).willReturn(emptyOrder);
            given(orderServiceMapper.toSummaryResultList(emptyOrder)).willReturn(dtoEmptyList);

            //when
            List<OrderSummaryResult> result = orderService.getOrderSummaries(MEMBER_ID);

            //then
            assertThat(result).isEqualTo(dtoEmptyList);
            then(memberRepository).should(times(1)).findById(MEMBER_ID);
            then(orderRepository).should(times(1)).findByMember(member);
            then(orderServiceMapper).should(times(1)).toSummaryResultList(emptyOrder);
            verifyNoInteractions(productRepository);
        }
    }

    @Nested
    @DisplayName("processPayment(Long, PayCommand)")
    class ProcessPayment {
        @Test
        @DisplayName("결제: 주문 조회 -> 결제 -> 매퍼 dto 변환")
        void success() {
            //given
            Order order = createSampleOrder();
            given(orderRepository.findById(1L)).willReturn(Optional.of(order));

            Pay pay = new Pay(PayMethod.CARD, order.getOrderAmount().getFinalAmount());
            PayCommand command = new PayCommand(PayMethod.CARD, order.getOrderAmount().getFinalAmount());

            given(payMapper.toEntity(command)).willReturn(pay);

            //when
            Pay result = orderService.processPayment(1L, command);

            //then
            assertSoftly(softly -> {
                softly.assertThat(result.getPayAmount()).isEqualTo(order.getOrderAmount().getFinalAmount());
            });

            then(orderRepository).should(times(1)).findById(1L);
            then(payMapper).should(times(1)).toEntity(command);
        }

        @Test
        @DisplayName("중복 결제 -> 예외")
        void shouldFail_whenDuplicatedPay() {
            //given
            Order order = createSampleOrder();
            given(orderRepository.findById(1L)).willReturn(Optional.of(order));

            Pay pay = new Pay(PayMethod.CARD, order.getOrderAmount().getFinalAmount());
            PayCommand command = new PayCommand(PayMethod.CARD, order.getOrderAmount().getFinalAmount());

            given(payMapper.toEntity(command)).willReturn(pay);

            orderService.processPayment(1L, command); // 첫 번째 결제

            //then
            assertThatThrownBy(() -> orderService.processPayment(1L, command))
                    .isInstanceOfSatisfying(OrderStatusException.class, e -> {
                        assertThat(e.getMessage()).contains(DomainType.ORDER.toString());
                        assertThat(e.getMessage()).contains("상태 오류");
                        assertThat(e.getMessage()).contains(OrderStatus.CONFIRMED.name().toUpperCase());
                    });

            then(orderRepository).should(times(2)).findById(1L);
            then(payMapper).should(times(2)).toEntity(command);
        }

        @Test
        @DisplayName("주문 금액, 결제 금액 불일치 -> 예외")
        void shouldFail_whenMismatchAmount() {
            //given
            Order order = createSampleOrder();
            given(orderRepository.findById(1L)).willReturn(Optional.of(order));

            int invalidAmount = 999_999;

            Pay pay = new Pay(PayMethod.CARD, invalidAmount);
            PayCommand command = new PayCommand(PayMethod.CARD, invalidAmount);

            given(payMapper.toEntity(command)).willReturn(pay);

            //then
            assertThatThrownBy(() -> orderService.processPayment(1L, command))
                    .isInstanceOfSatisfying(PayAmountMismatchException.class, e -> {
                        assertThat(e.getMessage()).contains("결제 금액");
                        assertThat(e.getMessage()).contains(String.valueOf(invalidAmount));
                        assertThat(e.getMessage()).contains(String.valueOf(order.getOrderAmount().getFinalAmount()));
                    });

            then(orderRepository).should(times(1)).findById(1L);
            then(payMapper).should(times(1)).toEntity(command);
        }

        private @NotNull Order createSampleOrder() {
            return Order.createOrder(member,
                    OrderItem.createOrderItem(book, 10),
                    OrderItem.createOrderItem(keyboard, 10));
        }
    }

    @Nested
    @DisplayName("prepareDelivery(Long, Address)")
    class PrepareDelivery {
        @Test
        @DisplayName("배송 준비: 결제된 주문 조회 -> delivery 생성 및 주소 세팅 -> address dto 변환 -> delivery dto 변환")
        void success() {
            //given
            Order order = createSampleOrder(member);

            processPayment(order);

            given(orderRepository.findById(1L)).willReturn(Optional.of(order));


            given(addressMapper.toDto(any(Address.class)))
                    .willAnswer(invocationOnMock -> {
                        Address addr = invocationOnMock.getArgument(0);
                        return new AddressDto(
                                addr.getPostcode(),
                                addr.getState(),
                                addr.getCity(),
                                addr.getStreet(),
                                addr.getDetail()
                        );
                    });

            given(deliveryServiceMapper.toDeliverySummary(any(Delivery.class)))
                    .willAnswer(invocationOnMock -> {
                        Delivery d = invocationOnMock.getArgument(0);
                        return new DeliverySummaryResult(
                                d.getDeliveryStatus(),
                                d.getTrackingNo(),
                                addressMapper.toDto(d.getShipAddr()),
                                null, null
                        );
                    });

            Address sampleAddr = createSampleAddr();

            //when
            DeliverySummaryResult result = orderService.prepareDelivery(1L, sampleAddr);

            //then
            assertSoftly(softly -> {
                softly.assertThat(result.deliveryStatus()).isEqualTo(DeliveryStatus.READY);
                softly.assertThat(result.trackingNo()).isNull();
                softly.assertThat(result.shipAddr().city()).isEqualTo(sampleAddr.getCity());
            });

            then(orderRepository).should(times(1)).findById(1L);
            then(addressMapper).should(times(1)).toDto(any(Address.class));
            then(deliveryServiceMapper).should(times(1)).toDeliverySummary(any(Delivery.class));
        }

        @Test
        @DisplayName("회원 주소 존재 / 배송 주소 없음: 결제된 주문 조회 -> 회원 주소 조회 -> delivery 생성 및 주소 세팅 -> address dto 변환 -> delivery dto 변환")
        void success_whenShipAddrIsNullAndMemberAddrIsNotNull() {
            //given
            Order order = createSampleOrder(member);

            processPayment(order);

            given(orderRepository.findById(1L)).willReturn(Optional.of(order));

            given(addressMapper.toDto(any(Address.class)))
                    .willAnswer(invocationOnMock -> {
                        Address addr = invocationOnMock.getArgument(0);
                        return new AddressDto(
                                addr.getPostcode(),
                                addr.getState(),
                                addr.getCity(),
                                addr.getStreet(),
                                addr.getDetail()
                        );
                    });

            given(deliveryServiceMapper.toDeliverySummary(any(Delivery.class)))
                    .willAnswer(invocationOnMock -> {
                        Delivery d = invocationOnMock.getArgument(0);
                        return new DeliverySummaryResult(
                                d.getDeliveryStatus(),
                                d.getTrackingNo(),
                                addressMapper.toDto(d.getShipAddr()),
                                null, null
                        );
                    });

            //when
            DeliverySummaryResult result = orderService.prepareDelivery(1L, null);

            //then
            assertSoftly(softly -> {
                softly.assertThat(result.deliveryStatus()).isEqualTo(DeliveryStatus.READY);
                softly.assertThat(result.trackingNo()).isNull();
                softly.assertThat(result.shipAddr().city()).isEqualTo(member.getAddr().getCity());
            });

            then(orderRepository).should(times(1)).findById(1L);
            then(addressMapper).should(times(1)).toDto(any(Address.class));
            then(deliveryServiceMapper).should(times(1)).toDeliverySummary(any(Delivery.class));
        }

        @Test
        @DisplayName("회원 주소 / 배송 주소 없음: 예외 InvalidAddressException")
        void shouldFail_whenShipAddrAndMemberAddrIsNull() {
            //given
            Order order = createSampleOrder(
                    Member.builder()
                            .loginId("user1")
                            .password("abc12345")
                            .name("차태승")
                            .email("cts9458@naver.com")
                            .addr(null)   // 회원 주소 null
                            .build());

            processPayment(order);

            given(orderRepository.findById(1L)).willReturn(Optional.of(order));

            //when-then
            assertThatThrownBy(() -> orderService.prepareDelivery(1L, null))
                    .isInstanceOfSatisfying(InvalidAddressException.class, e -> {
                        assertThat(e.getReason()).isEqualTo(InvalidAddressException.Reason.REQUIRED);
                    });

            then(orderRepository).should(times(1)).findById(1L);
            verifyNoInteractions(addressMapper, deliveryServiceMapper);
        }

    }

    @Nested
    @DisplayName("startDelivery(Long, String, LocalDatetime)")
    class StartDelivery {

        String trackingNo = "98765";
        LocalDateTime shippedAt = LocalDateTime.of(2025, 11, 12, 8, 30);

        @Test
        @DisplayName("배송 시작: 배송 준비된 주문 조회 -> 운송장 번호, 배송 시작 시간 설정")
        void success() {
            //given
            long orderId = 123L;
            Order order = createSampleOrder(member);
            given(orderRepository.findById(orderId)).willReturn(Optional.of(order));

            processPayment(order);

            Address shipAddr = createSampleAddr();
            orderService.prepareDelivery(orderId, shipAddr);

            //when
            orderService.startDelivery(orderId, trackingNo, null);
            Delivery delivery = orderRepository.findById(orderId).get().getDelivery();

            //then
            assertSoftly(softly -> {
                softly.assertThat(delivery.getDeliveryStatus()).isEqualTo(DeliveryStatus.SHIPPING);
                softly.assertThat(delivery.getTrackingNo()).isEqualTo(trackingNo);
                softly.assertThat(delivery.getShippedAt()).isNotNull();
            });
        }

        @Test
        @DisplayName("배송 시작 시간 null -> 배송 시작 시간 지금 시간으로 설정")
        void success_whenShippedAtIsNull() {
            //given
            long orderId = 123L;
            Order order = createSampleOrder(member);
            given(orderRepository.findById(orderId)).willReturn(Optional.of(order));

            processPayment(order);

            Address shipAddr = createSampleAddr();
            orderService.prepareDelivery(orderId, shipAddr);

            //when
            orderService.startDelivery(orderId, trackingNo, shippedAt);
            Delivery delivery = orderRepository.findById(orderId).get().getDelivery();

            //then
            assertSoftly(softly -> {
                softly.assertThat(delivery.getDeliveryStatus()).isEqualTo(DeliveryStatus.SHIPPING);
                softly.assertThat(delivery.getTrackingNo()).isEqualTo(trackingNo);
                softly.assertThat(delivery.getShippedAt()).isEqualTo(shippedAt);
            });
        }

        @Test
        @DisplayName("결제 되지 않은 상태 -> 예외")
        void shouldFail_whenNotPaid() {
            //given
            long orderId = 123L;
            Order order = createSampleOrder(member);
            given(orderRepository.findById(orderId)).willReturn(Optional.of(order));

            //when-then
            assertThatThrownBy(() -> orderService.startDelivery(orderId, trackingNo, shippedAt))
                    .isInstanceOf(DeliveryException.class);
        }

        @Test
        @DisplayName("배송 준비 되지 않은 상태 -> 예외")
        void shouldFail_whenNotPrepared() {
            //given
            long orderId = 123L;
            Order order = createSampleOrder(member);
            given(orderRepository.findById(orderId)).willReturn(Optional.of(order));

            processPayment(order);

            //when-then
            assertThatThrownBy(() -> orderService.startDelivery(orderId, trackingNo, shippedAt))
                    .isInstanceOf(DeliveryException.class);
        }
    }

    @Nested
    @DisplayName("completeDelivery(Long, LocalDatetime)")
    class CompleteDelivery {

        LocalDateTime arrivedAt = LocalDateTime.of(2025, 12, 1, 8, 30);

        @Test
        @DisplayName("배송 완료: 배송 중인 주문 운송장 번호로 조회 -> 도착 시간 설정")
        void success() {
            //given
            Order order = createSampleOrder(member);
            Long orderId = order.getId();

            processPayment(order);

            startDelivery(order);

            given(orderRepository.findById(orderId)).willReturn(Optional.of(order));

            //when
            orderService.completeDelivery(orderId, arrivedAt);
            Delivery delivery = order.getDelivery();

            //then
            assertSoftly(softly -> {
                softly.assertThat(delivery.getDeliveryStatus()).isEqualTo(DeliveryStatus.COMPLETED);
                softly.assertThat(delivery.getArrivedAt()).isEqualTo(arrivedAt);
            });

            then(orderRepository).should(times(3)).findById(orderId);
        }

        @Test
        @DisplayName("도착 시간 null -> 지금 시간으로 설정")
        void success_whenArrivedAtIsNull() {
            //given
            Order order = createSampleOrder(member);
            Long orderId = order.getId();

            processPayment(order);

            startDelivery(order);

            given(orderRepository.findById(orderId)).willReturn(Optional.of(order));

            //when
            orderService.completeDelivery(orderId, null);
            Delivery delivery = order.getDelivery();

            //then
            assertSoftly(softly -> {
                softly.assertThat(delivery.getDeliveryStatus()).isEqualTo(DeliveryStatus.COMPLETED);
                softly.assertThat(delivery.getArrivedAt()).isNotNull();
            });

            then(orderRepository).should(times(3)).findById(orderId);
        }

        @Test
        @DisplayName("결제 되지 않은 상태 -> 예외")
        void shouldFail_whenNotPaid() {
            //given
            Order order = createSampleOrder(member);
            Long orderId = order.getId();
            given(orderRepository.findById(orderId)).willReturn(Optional.of(order));

            //when-then
            assertThatThrownBy(() -> orderService.completeDelivery(orderId, arrivedAt))
                    .isInstanceOf(DeliveryException.class);
        }

        @Test
        @DisplayName("배송 준비 되지 않은 상태 -> 예외")
        void shouldFail_whenNotPrepared() {
            //given
            Order order = createSampleOrder(member);
            Long orderId = order.getId();
            given(orderRepository.findById(orderId)).willReturn(Optional.of(order));

            processPayment(order);

            //when-then
            assertThatThrownBy(() -> orderService.completeDelivery(orderId, arrivedAt))
                    .isInstanceOf(DeliveryException.class);
        }

        @Test
        @DisplayName("배송 시작하지 않은 상태 -> 예외")
        void shouldFail_whenNotShipping() {
            //given
            Order order = createSampleOrder(member);
            Long orderId = order.getId();
            given(orderRepository.findById(orderId)).willReturn(Optional.of(order));

            processPayment(order);

            prepareDelivery(order);

            //when-then
            assertThatThrownBy(() -> orderService.completeDelivery(orderId, arrivedAt))
                    .isInstanceOf(DeliveryStatusException.class);
        }

        private void startDelivery(Order order) {
            Long orderId = prepareDelivery(order);
            orderService.startDelivery(orderId, "12345", arrivedAt);
        }

        private @NotNull Long prepareDelivery(Order order) {
            Long orderId = order.getId();
            given(orderRepository.findById(orderId)).willReturn(Optional.of(order));
            Address shipAddr = createSampleAddr();
            orderService.prepareDelivery(orderId, shipAddr);
            return orderId;
        }
    }


    private @NotNull Order createSampleOrder(Member member) {
        return Order.createOrder(member,
                OrderItem.createOrderItem(book, 10),
                OrderItem.createOrderItem(keyboard, 10));
    }

    private static void processPayment(Order order) {
        Pay pay = new Pay(PayMethod.CARD, order.getOrderAmount().getFinalAmount());
        order.processPayment(pay);
    }



    private Address createSampleAddr() {
        return Address.createAddress(
                "10580",
                "서울특별시",
                "노원구",
                "노원동",
                "상가 1층"
        );
    }
}