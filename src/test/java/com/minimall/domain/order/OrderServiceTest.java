package com.minimall.domain.order;

import com.minimall.domain.embeddable.Address;
import com.minimall.domain.member.Member;
import com.minimall.domain.member.MemberRepository;
import com.minimall.domain.order.dto.OrderMapper;
import com.minimall.domain.order.dto.request.OrderCreateRequestDto;
import com.minimall.domain.order.dto.request.OrderItemCreateDto;
import com.minimall.domain.order.status.OrderStatus;
import com.minimall.domain.product.Product;
import com.minimall.domain.product.ProductRepository;
import com.minimall.service.OrderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
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
    private OrderItemCreateDto orderItemCreateRequest1;
    private OrderItemCreateDto orderItemCreateRequest2;
    private List<OrderItemCreateDto> orderItems = new ArrayList<>();

    private Member member;
    private Product product1;
    private Product product2;

    private final Long MEMBER_ID = 100L;
    private final Long PRODUCT1_ID = 1L;
    private final Long PRODUCT2_ID = 2L;



    @BeforeEach
    void setUp() {
        //== OrderCreateRequest ==//
        orderCreateRequest = new OrderCreateRequestDto(100L, orderItems);

        //== OrderItemRequestList ==//
        orderItemCreateRequest1 = new OrderItemCreateDto(1L, 30);
        orderItemCreateRequest2 = new OrderItemCreateDto(2L, 10);
        orderItems.add(orderItemCreateRequest1);
        orderItems.add(orderItemCreateRequest2);

        //== Member Entity ==//
        member = Member.builder()
                .loginId("user1")
                .password("abc12345")
                .name("차태승")
                .email("cts9458@naver.com")
                .addr(new Address("12345", "광주광역시", "광산구", "수등로76번길 40", "123동 1501호"))
                .build();

        //== Product Entity ==//
        product1 = new Product("도서<클린 코드>", 20000, 50);
        product2 = new Product("키보드", 100000, 20);
    }

    @Test
    void createOrder_success() {
        //given
        given(memberRepository.findById(MEMBER_ID)).willReturn(Optional.of(member));
        given(productRepository.findById(PRODUCT1_ID)).willReturn(Optional.of(product1));
        given(productRepository.findById(PRODUCT2_ID)).willReturn(Optional.of(product2));
        given(orderRepository.save(any(Order.class))).willAnswer(invocation -> invocation.getArgument(0));

        //when
        Long orderId = orderService.createOrder(orderCreateRequest);

        //then: 호출검증
        then(memberRepository).should(times(1)).findById(MEMBER_ID);
        then(productRepository).should(times(2)).findById(anyLong());
        then(orderRepository).should(times(1)).save(any(Order.class));

        //then: 저장된 Order 캡처
        ArgumentCaptor<Order> captor = ArgumentCaptor.forClass(Order.class);
        then(orderRepository).should().save(captor.capture());
        Order savedOrder = captor.getValue();

        //then: 주문 기본 정보 검증
        assertThat(savedOrder.getOrderStatus()).isEqualTo(OrderStatus.ORDERED);
        assertThat(savedOrder.getOrderedAt()).isNotNull();

        //then: 가격 계산 검증
        int expectedAmount = (orderItemCreateRequest1.quantity() * product1.getPrice())
                + (orderItemCreateRequest2.quantity() * product2.getPrice());
        assertThat(savedOrder.getOrderAmount().getOriginalAmount()).isEqualTo(expectedAmount);
        assertThat(savedOrder.getOrderAmount().getFinalAmount()).isEqualTo(expectedAmount);

        //then: 연관관계 검증
        assertThat(savedOrder.getMember()).isEqualTo(member);
        assertThat(member.getOrders()).containsExactly(savedOrder);
        assertThat(savedOrder.getDelivery()).isNull();
        assertThat(savedOrder.getPay()).isNull();
        assertThat(savedOrder.getOrderItems()).hasSize(2);
        assertThat(savedOrder.getOrderItems())
                .extracting(OrderItem::getProduct)
                .containsExactlyInAnyOrder(product1, product2);


        //then: 상품 재고 감소 검증
        assertThat(product1.getStockQuantity()).isEqualTo(50 - 30);
        assertThat(product2.getStockQuantity()).isEqualTo(20 - 10);
    }
}