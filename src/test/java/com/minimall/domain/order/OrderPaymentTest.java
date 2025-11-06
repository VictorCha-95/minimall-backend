package com.minimall.domain.order;

import com.minimall.domain.common.DomainType;
import com.minimall.domain.embeddable.Address;
import com.minimall.domain.exception.DomainExceptionMessage;
import com.minimall.domain.member.Member;
import com.minimall.domain.order.exception.OrderStatusException;
import com.minimall.domain.order.pay.PayAmountMismatchException;
import com.minimall.domain.order.pay.PayMethod;
import com.minimall.domain.order.pay.PayStatus;
import com.minimall.domain.product.Product;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.assertj.core.api.SoftAssertions.*;

public class OrderPaymentTest {

    Order order;

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

        //== Order Entity ==//
        order = Order.createOrder(member, orderItems.toArray(OrderItem[]::new));
    }

    @Nested
    @DisplayName("processPayment(Pay)")
    class ProcessPayment {
        @Test
        @DisplayName("정상 -> 결제 처리")
        void success() {
            //given
            Pay pay = new Pay(PayMethod.CARD, totalPrice());

            //when
            order.processPayment(pay);

            //then
            assertSoftly(softly -> {
                softly.assertThat(order.getPay()).isEqualTo(pay);
                softly.assertThat(order.getOrderStatus()).isEqualTo(OrderStatus.CONFIRMED);
                softly.assertThat(pay.getOrder()).isEqualTo(order);
                softly.assertThat(pay.getPayStatus()).isEqualTo(PayStatus.PAID);
                softly.assertThat(pay.getPaidAt()).isNotNull();
            });
        }

        @Test
        @DisplayName("pay null -> 예외")
        void shouldFail_whenPayIsNull() {
            assertThatThrownBy(() -> order.processPayment(null))
                    .isInstanceOfSatisfying(IllegalArgumentException.class, e -> {
                        assertThat(e.getMessage()).isEqualTo(DomainExceptionMessage.PARAM_REQUIRE_NOT_NULL.text("pay"));
                        assertThat(order.getOrderStatus()).isEqualTo(OrderStatus.ORDERED);
                    });
        }

        @Test
        @DisplayName("동일한 결제로 중복 결제 -> 예외(결제 일자 불변)")
        void shouldFail_whenDuplicatedPay() {
            //given
            Pay pay = new Pay(PayMethod.CARD, totalPrice());

            //when
            order.processPayment(pay);
            LocalDateTime paidAt = pay.getPaidAt();

            //then
            assertThatThrownBy(() -> order.processPayment(pay))
                    .isInstanceOfSatisfying(OrderStatusException.class, e -> {
                        assertThat(e.getDomain()).isEqualTo(DomainType.ORDER);
                        assertThat(e.getCurrentStatus()).isEqualTo(OrderStatus.CONFIRMED);
                        assertThat(e.getTargetStatus()).isEqualTo(OrderStatus.CONFIRMED);
                        assertThat(pay.getPaidAt()).isEqualTo(paidAt);
                    });
        }

        @Test
        @DisplayName("다른 결제로 중복 결제 -> 예외")
        void shouldFail_whenDuplicatedAnotherPay() {
            //given
            Pay pay = new Pay(PayMethod.CARD, totalPrice());
            Pay anotherPay = new Pay(PayMethod.MOBILE_PAY, totalPrice());


            //when
            order.processPayment(pay);
            LocalDateTime paidAt = pay.getPaidAt();

            //then
            assertThatThrownBy(() -> order.processPayment(anotherPay))
                    .isInstanceOfSatisfying(OrderStatusException.class, e -> {
                        assertThat(e.getDomain()).isEqualTo(DomainType.ORDER);
                        assertThat(e.getCurrentStatus()).isEqualTo(OrderStatus.CONFIRMED);
                        assertThat(e.getTargetStatus()).isEqualTo(OrderStatus.CONFIRMED);
                        assertThat(pay.getPaidAt()).isEqualTo(paidAt);
                    });
        }

        @Test
        @DisplayName("취소된 상태에서 결제 -> 예외")
        void shouldFail_whenCanceledPay() {
            //given
            Pay pay = new Pay(PayMethod.CARD, totalPrice());

            //when
            order.processPayment(pay);
            order.cancel();

            //then
            assertThatThrownBy(() -> order.processPayment(pay))
                    .isInstanceOfSatisfying(OrderStatusException.class, e -> {
                        assertThat(e.getDomain()).isEqualTo(DomainType.ORDER);
                        assertThat(e.getCurrentStatus()).isEqualTo(OrderStatus.CANCELED);
                        assertThat(e.getTargetStatus()).isEqualTo(OrderStatus.CONFIRMED);
                    });
        }

        @Test
        @DisplayName("주문 금액 != 결제 금액 -> 예외")
        void shouldFail_whenMismatchPayAmount() {
            //given
            Pay pay = new Pay(PayMethod.CASH, totalPrice() + 1);

            //then
            assertThatThrownBy(() -> order.processPayment(pay))
                    .isInstanceOfSatisfying(PayAmountMismatchException.class, e -> {
                        assertThat(e.getMessage()).contains("결제 금액 불일치");
                        assertThat(e.getMessage()).contains(String.valueOf(totalPrice()));
                        assertThat(e.getMessage()).contains(String.valueOf(totalPrice() + 1));
                        assertThat(pay.getPayStatus()).isEqualTo(PayStatus.FAILED);
                        assertThat(order.getOrderStatus()).isEqualTo(OrderStatus.ORDERED);
                        assertThat(pay.getPaidAt()).isNull();
                    });
        }
    }

}
