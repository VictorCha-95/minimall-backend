package com.minimall.domain.order;

import com.minimall.domain.embeddable.Address;
import com.minimall.domain.member.Member;
import com.minimall.domain.order.exception.InvalidOrderItemException;
import com.minimall.domain.product.Product;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static com.minimall.domain.order.OrderMessage.*;
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

    private int totalPrice() {
        return (keyboardOrderQuantity * keyboardPrice) + (mouseOrderQuantity * mousePrice);
    }

    private static final String DEFAULT_LOGIN_ID = "user123";
    private static final String DEFAULT_PASSWORD_HASH = "12345678";
    private static final String DEFAULT_NAME = "차태승";
    private static final String DEFAULT_EMAIL = "user123@example.com";
    private static final Address DEFAULT_ADDRESS =
            Address.createAddress("62550", "광주광역시", "광산구", "수등로76번길 40", "123동 456호");


    @BeforeEach
    void setUp() {
        //== Member Entity ==//
        member = Member.registerCustomer(DEFAULT_LOGIN_ID, DEFAULT_PASSWORD_HASH, DEFAULT_NAME, DEFAULT_EMAIL, DEFAULT_ADDRESS);

        //== Product Entity ==//
        keyboard = new Product("키보드", keyboardPrice, keyboardStock);
        mouse = new Product("마우스", mousePrice, mouseStock);

        //== OrderItem List ==//
        orderItems.add(OrderItem.createOrderItem(keyboard, keyboardOrderQuantity));
        orderItems.add(OrderItem.createOrderItem(mouse, mouseOrderQuantity));
    }

    @Test
    @DisplayName("정상 -> 생성")
    void success() {
        //when
        Order order = Order.createOrder(member, orderItems.toArray(OrderItem[]::new));

        //then: 연관관계 검증
        assertThat(order.getPay()).isNull();
        assertThat(order.getDelivery()).isNull();

        //then: 기본 정보 검증
        assertThat(order.getOrderedAt()).isNotNull();
        assertThat(order.getOrderStatus()).isEqualTo(OrderStatus.ORDERED);

        //then: orderItem 합산 가격 검증(주문 가격 검증)
        assertThat(order.getOrderAmount().getOriginalAmount())
                .isEqualTo(totalPrice());
        assertThat(order.getOrderAmount().getFinalAmount())
                .isEqualTo(totalPrice());
    }

    @Nested
    class MemberArg {
        @Test
        @DisplayName("정상 -> 연관관계 추가")
        void success() {
            //when
            Order order = Order.createOrder(member, orderItems.toArray(OrderItem[]::new));

            //then
            assertThat(order.getMember()).isEqualTo(member);
            assertThat(member.getOrders()).containsExactly(order);
        }

        @Test
        @DisplayName("null -> 예외")
        void shouldFail_whenMemberIsNull() {
            NullPointerException ex = assertThrows(NullPointerException.class, () -> Order.createOrder(null, orderItems.toArray(OrderItem[]::new)));
            assertThat(ex.getMessage()).isEqualTo(MEMBER_REQUIRED_FOR_ORDER_CREATION.text());
        }
    }

    @Nested
    class OrderItemArg {
        @Test
        @DisplayName("정상 -> 연관관계 추가, 주문 항목 불변")
        void success() {
            //when
            Order order = Order.createOrder(member, orderItems.toArray(OrderItem[]::new));

            //then
            assertThat(order.getOrderItems()).allSatisfy(oi -> assertThat(oi.getOrder()).isEqualTo(order));
            assertThat(order.getOrderItems())
                    .extracting(OrderItem::getOrderQuantity)
                    .containsExactlyInAnyOrder(keyboardOrderQuantity, mouseOrderQuantity);
            assertThat(order.getOrderItems())
                    .extracting(OrderItem::getProductName)
                    .containsExactlyInAnyOrder("키보드", "마우스");
            assertThrows(UnsupportedOperationException.class, () -> order.getOrderItems().add(orderItems.getFirst()));
        }

        @Test
        @DisplayName("null -> 예외")
        void shouldFail_whenOrderItemIsNull() {
            assertThatThrownBy(() -> Order.createOrder(member))
                    .isInstanceOfSatisfying(InvalidOrderItemException.class, e -> {
                        assertThat(e.getReason()).isEqualTo(InvalidOrderItemException.Reason.REQUIRE_ITEM);
                        assertThat(e.getMessage())
                                .isEqualTo(OrderMessage.REQUIRE_ORDER_ITEM.text());
                    });

            //when, then
            assertThrows(InvalidOrderItemException.class,
                    () -> Order.createOrder(member, new OrderItem[3]));
            assertThrows(InvalidOrderItemException.class,
                    () -> Order.createOrder(member, new OrderItem[]{orderItems.getFirst(), null}));
        }

        @Test
        @DisplayName("길이 0 배열 -> 예외")
        void shouldFail_whenArrayLengthIsZero() {
            assertThatThrownBy(() -> Order.createOrder(member, new OrderItem[]{}))
                    .isInstanceOfSatisfying(InvalidOrderItemException.class, e -> {
                        assertThat(e.getReason()).isEqualTo(InvalidOrderItemException.Reason.REQUIRE_ITEM);
                    });
        }

        @Test
        @DisplayName("빈 배열 -> 예외")
        void shouldFail_whenArrayIsEmpty() {
            assertThatThrownBy(() -> Order.createOrder(member, new OrderItem[3]))
                    .isInstanceOfSatisfying(InvalidOrderItemException.class, e -> {
                        assertThat(e.getReason()).isEqualTo(InvalidOrderItemException.Reason.CONTAIN_NULL_ITEM);
                        assertThat(e.getIndex()).isEqualTo(0);
                    });
        }

        @Test
        @DisplayName("null 포함 -> 예외")
        void shouldFail_whenContainNull() {
            assertThatThrownBy(() -> Order.createOrder(member, new OrderItem[]{orderItems.getFirst(), null}))
                    .isInstanceOfSatisfying(InvalidOrderItemException.class, e -> {
                        assertThat(e.getReason()).isEqualTo(InvalidOrderItemException.Reason.CONTAIN_NULL_ITEM);
                        assertThat(e.getIndex()).isEqualTo(1);
                    });
        }
    }
}