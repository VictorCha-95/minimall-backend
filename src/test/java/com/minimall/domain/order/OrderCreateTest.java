package com.minimall.domain.order;

import com.minimall.domain.embeddable.Address;
import com.minimall.domain.member.Member;
import com.minimall.domain.order.exception.EmptyOrderItemException;
import com.minimall.domain.order.status.OrderStatus;
import com.minimall.domain.product.Product;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static com.minimall.domain.order.message.OrderMessage.*;
import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Order.createOrder()")
public class OrderCreateTest {

    Member member;

    List<OrderItem> orderItems = new ArrayList<>();

    Product keyboard;
    Product mouse;

    int keyboardPrice = 50000;
    int keyboardStock = 50;
    int keyboardOrderQuantity = 30;

    int mousePrice = 20000;
    int mouseStock = 20;
    int mouseOrderQuantity = 10;


    @BeforeEach
    void setUp() {
        //== Member Entity ==//
        member = Member.builder()
                .loginId("user123")
                .password("12345")
                .email("user123@example.com")
                .name("차태승")
                .addr(Address.createAddress("62550", "광주광역시", "광산구", "수등로76번길 40", "123동 456호"))
                .build();

        //== Product Entity ==//
        keyboard = new Product("키보드", keyboardPrice, keyboardStock);
        mouse = new Product("마우스", mousePrice, mouseStock);

        //== OrderItem List ==//
        orderItems.add(OrderItem.createOrderItem(keyboard, keyboardOrderQuantity));
        orderItems.add(OrderItem.createOrderItem(mouse, mouseOrderQuantity));
    }

    @Test
    @DisplayName("주문 생성 성공")
    void createOrder_success() {
        //when
        Order order = Order.createOrder(member, orderItems.toArray(OrderItem[]::new));

        //then: 연관관계 검증
        assertThat(order.getMember()).isEqualTo(member);
        assertThat(member.getOrders()).containsExactly(order);
        assertThat(order.getOrderItems()).allSatisfy(oi -> assertThat(oi.getOrder()).isEqualTo(order));
        assertThat(order.getOrderItems())
                .extracting(OrderItem::getOrderQuantity)
                .containsExactlyInAnyOrder(keyboardOrderQuantity, mouseOrderQuantity);
        assertThat(order.getOrderItems())
                .extracting(OrderItem::getProductName)
                .containsExactlyInAnyOrder("키보드", "마우스");
        assertThat(order.getPay()).isNull();
        assertThat(order.getDelivery()).isNull();

        //then: 주문 항목 불변 검증
        assertThrows(UnsupportedOperationException.class, () -> order.getOrderItems().add(orderItems.getFirst()));

        //then: 기본 정보 검증
        assertThat(order.getOrderedAt()).isNotNull();
        assertThat(order.getOrderStatus()).isEqualTo(OrderStatus.ORDERED);

        //then: orderItem 합산 가격 검증(주문 가격 검증)
        assertThat(order.getOrderAmount().getOriginalAmount())
                .isEqualTo((keyboardOrderQuantity * keyboardPrice) + (mouseOrderQuantity * mousePrice));
        assertThat(order.getOrderAmount().getFinalAmount())
                .isEqualTo((keyboardOrderQuantity * keyboardPrice) + (mouseOrderQuantity * mousePrice));
    }

    @Test
    @DisplayName("주문 항목 생성 시 상품 재고 차감")
    void shouldReduceStock_whenCreateOrderItem() {
        //then
        assertThat(keyboard.getStockQuantity()).isEqualTo(keyboardStock - keyboardOrderQuantity);
        assertThat(mouse.getStockQuantity()).isEqualTo(mouseStock - mouseOrderQuantity);
    }

    @Test
    @DisplayName("회원 정보 없이 주문 생성 시도하면 예외 발생")
    void shouldThrowNullPointerException_whenMemberIsNull() {
        //when, then
        NullPointerException ex = assertThrows(NullPointerException.class, () -> Order.createOrder(null, orderItems.toArray(OrderItem[]::new)));
        assertThat(ex.getMessage()).isEqualTo(MEMBER_REQUIRED_FOR_ORDER_CREATION.text());
    }

    @Test
    @DisplayName("주문 항목 중 하나라도 값이 비어 있으면 예외 발생")
    void shouldThrowEmptyOrderItemException_whenOrderItemIsNull() {
        //when, then
        EmptyOrderItemException ex = assertThrows(EmptyOrderItemException.class,
                () -> Order.createOrder(member));
        assertThrows(EmptyOrderItemException.class,
                () -> Order.createOrder(member, new OrderItem[]{}));
        assertThrows(EmptyOrderItemException.class,
                () -> Order.createOrder(member, new OrderItem[3]));
        assertThrows(EmptyOrderItemException.class,
                () -> Order.createOrder(member, new OrderItem[]{orderItems.getFirst(), null}));
        assertThat(ex.getMessage()).isEqualTo(EMPTY_ORDER_ITEM.text());
    }
}