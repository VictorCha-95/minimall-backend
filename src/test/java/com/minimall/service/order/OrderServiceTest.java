package com.minimall.service.order;

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
import com.minimall.domain.product.Product;
import com.minimall.domain.product.ProductRepository;
import com.minimall.service.OrderService;
import com.minimall.service.exception.MemberNotFoundException;
import com.minimall.service.exception.ProductNotFoundException;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.BDDMockito;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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

}