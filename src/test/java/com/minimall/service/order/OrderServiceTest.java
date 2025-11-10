package com.minimall.service.order;

import com.minimall.domain.common.DomainType;
import com.minimall.domain.embeddable.Address;
import com.minimall.domain.member.Member;
import com.minimall.domain.member.MemberRepository;
import com.minimall.domain.order.Order;
import com.minimall.domain.order.OrderItem;
import com.minimall.domain.order.OrderRepository;
import com.minimall.domain.order.dto.OrderMapper;
import com.minimall.domain.order.dto.request.OrderCreateRequestDto;
import com.minimall.domain.order.dto.request.OrderItemCreateDto;
import com.minimall.domain.order.OrderStatus;
import com.minimall.domain.order.dto.response.OrderDetailResponseDto;
import com.minimall.domain.order.dto.response.OrderItemResponseDto;
import com.minimall.domain.order.dto.response.OrderSummaryResponseDto;
import com.minimall.domain.product.Product;
import com.minimall.domain.product.ProductRepository;
import com.minimall.service.OrderService;
import com.minimall.service.exception.MemberNotFoundException;
import com.minimall.service.exception.OrderNotFoundException;
import com.minimall.service.exception.ProductNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.swing.text.html.Option;
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
    OrderMapper orderMapper;

    @InjectMocks
    OrderService orderService;

    private OrderCreateRequestDto orderCreateRequest;
    private List<OrderItemResponseDto> orderItemResponses;

    private Member member;
    private Product book;
    private Product keyboard;

    private final Long MEMBER_ID = 100L;
    private final Long PRODUCT1_ID = 1L;
    private final Long PRODUCT2_ID = 2L;



    @BeforeEach
    void setUp() {
        //== OrderItemRequestList ==//
        List<OrderItemCreateDto> orderItems = new ArrayList<>();

        OrderItemCreateDto orderItemCreateRequest1 = new OrderItemCreateDto(PRODUCT1_ID, 30);
        OrderItemCreateDto orderItemCreateRequest2 = new OrderItemCreateDto(PRODUCT2_ID, 10);
        orderItems.add(orderItemCreateRequest1);
        orderItems.add(orderItemCreateRequest2);

        //== OrderCreateRequest ==//
        orderCreateRequest = new OrderCreateRequestDto(MEMBER_ID, orderItems);

        //== OrderItemResponse ==//
        orderItemResponses = List.of(
                new OrderItemResponseDto(PRODUCT1_ID, "도서", 20000, 30, 600000),
                new OrderItemResponseDto(PRODUCT2_ID, "키보드", 100_000, 10, 1_000_000)
        );


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
    }

    @Nested
    @DisplayName("createOrder(CreateRequestDto)")
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
            orderService.createOrder(orderCreateRequest);

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
            assertThatThrownBy(() -> orderService.createOrder(orderCreateRequest))
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
            assertThatThrownBy(() -> orderService.createOrder(orderCreateRequest))
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
    @DisplayName("getOrderDetail(Long)")
    class GetOrderDetail{
        @Test
        @DisplayName("주문 상세 단건 조회: 리포지토리 조회 -> 매버 변환 -> dto 반환")
        void success() {
            //given
            Long orderId = 1L;

            Order order = mock(Order.class);

            LocalDateTime localDateTime = LocalDateTime.of(2025, 11, 10, 15, 25, 0);
            OrderDetailResponseDto dto = new OrderDetailResponseDto(orderId,
                    localDateTime,
                    OrderStatus.ORDERED,
                    1_100_000,
                    List.of(), null, null);

            given(orderRepository.findById(orderId)).willReturn(Optional.of(order));
            given(orderMapper.toOrderDetailResponse(order)).willReturn(dto);

            //when
            OrderDetailResponseDto result = orderService.getOrderDetail(orderId);

            //then
            assertThat(result).isEqualTo(dto);
            then(orderRepository).should(times(1)).findById(orderId);
            then(orderMapper).should(times(1)).toOrderDetailResponse(order);
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
            then(orderMapper).shouldHaveNoInteractions();
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

            List<OrderSummaryResponseDto> dtoList = List.of(new OrderSummaryResponseDto(
                    orderId,
                    localDateTime,
                    OrderStatus.ORDERED,
                    2,
                    1_100_000), new OrderSummaryResponseDto(
                    orderId,
                    localDateTime,
                    OrderStatus.ORDERED,
                    5,
                    5_500_000
            ));

            given(memberRepository.findById(MEMBER_ID)).willReturn(Optional.of(member));
            given(orderRepository.findByMember(member)).willReturn(orders);
            given(orderMapper.toOrderSummaryResponse(orders)).willReturn(dtoList);

            //when
            List<OrderSummaryResponseDto> result = orderService.getOrderSummaries(MEMBER_ID);

            //then
            assertThat(result).isEqualTo(dtoList);
            then(memberRepository).should(times(1)).findById(MEMBER_ID);
            then(orderRepository).should(times(1)).findByMember(member);
            then(orderMapper).should(times(1)).toOrderSummaryResponse(orders);
            verifyNoInteractions(productRepository);
        }

        @Test
        @DisplayName("회원 주문 없음: 빈 리스트 반환")
        void returnEmpty_whenOrderIsEmpty() {
            //given
            List<Order> emptyOrder = List.of();

            List<OrderSummaryResponseDto> dtoEmptyList = List.of();

            given(memberRepository.findById(MEMBER_ID)).willReturn(Optional.of(member));
            given(orderRepository.findByMember(member)).willReturn(emptyOrder);
            given(orderMapper.toOrderSummaryResponse(emptyOrder)).willReturn(dtoEmptyList);

            //when
            List<OrderSummaryResponseDto> result = orderService.getOrderSummaries(MEMBER_ID);

            //then
            assertThat(result).isEqualTo(dtoEmptyList);
            then(memberRepository).should(times(1)).findById(MEMBER_ID);
            then(orderRepository).should(times(1)).findByMember(member);
            then(orderMapper).should(times(1)).toOrderSummaryResponse(emptyOrder);
            verifyNoInteractions(productRepository);
        }
    }

}