package com.minimall.domain.order;

import com.minimall.domain.common.DomainType;
import com.minimall.domain.embeddable.Address;
import com.minimall.domain.member.Member;
import com.minimall.domain.order.delivery.DeliveryException;
import com.minimall.domain.order.delivery.DeliveryStatus;
import com.minimall.domain.order.delivery.DeliveryStatusException;
import com.minimall.domain.order.exception.PaymentRequiredException;
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

public class OrderDeliveryTest {

    Member member;

    Order order;

    List<OrderItem> orderItems = new ArrayList<>();

    Pay pay;

    Address shipAddr;
    final String trackingNo = "12345678";
    final LocalDateTime shippedAt = LocalDateTime.of(2025, 11, 10, 12, 30);
    final LocalDateTime arrivedAt = LocalDateTime.of(2025, 11, 20, 12, 30);


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
        Product keyboard = new Product("키보드", 50000, 50);
        Product mouse = new Product("마우스", 20000, 20);

        //== OrderItem List ==//
        orderItems.add(OrderItem.createOrderItem(keyboard, 30));
        orderItems.add(OrderItem.createOrderItem(mouse, 10));

        //== Order Entity ==//
        order = Order.createOrder(member, orderItems.toArray(OrderItem[]::new));

        //== Pay Entity ==//
        pay = new Pay(PayMethod.CARD, keyboard.getPrice() * 30 + mouse.getPrice() * 10);

        //== Address ==//
        shipAddr = new Address("12345", "광주광역시", "광산구", "신창동", "건물");
    }

    @Nested
    @DisplayName("prepareDelivery(Address)")
    class PrepareDelivery {
        @Test
        @DisplayName("정상 -> 배송 준비")
        void success() {
            //given
            order.processPayment(pay);

            //when
            order.prepareDelivery(shipAddr);

            //then
            assertSoftly(softly -> {
                softly.assertThat(order.getDelivery().getDeliveryStatus()).isEqualTo(DeliveryStatus.READY);
                softly.assertThat(order.getDelivery().getOrder()).isEqualTo(order);
                softly.assertThat(order.getDelivery().getShipAddr()).isEqualTo(shipAddr);
                softly.assertThat(order.getOrderStatus()).isEqualTo(OrderStatus.CONFIRMED);
            });
        }

        @Test
        @DisplayName("배송 주소 null -> 회원 주소 사용")
        void success_ifNotAssignedShipAddr_useMemberAddr() {
            //given
            order.processPayment(pay);

            //when
            order.prepareDelivery(member.getAddr());

            //then
            assertSoftly(softly -> {
                softly.assertThat(order.getDelivery().getShipAddr()).isEqualTo(member.getAddr());
                softly.assertThat(order.getDelivery().getDeliveryStatus()).isEqualTo(DeliveryStatus.READY);
                softly.assertThat(order.getOrderStatus()).isEqualTo(OrderStatus.CONFIRMED);
            });
        }

        @Test
        @DisplayName("결제 전 -> 예외")
        void shouldFail_BeforePayment() {
            assertThatThrownBy(() -> order.prepareDelivery(shipAddr))
                    .isInstanceOf(PaymentRequiredException.class);
        }

        @Test
        @DisplayName("취소된 배송 -> 예외")
        void shouldFail_whenStatusIsCanceled() {
            //given
            order.processPayment(pay);

            //when
            order.cancel();

            //then
            assertThatThrownBy(() -> order.prepareDelivery(shipAddr))
                    .isInstanceOfSatisfying(PaymentRequiredException.class, e ->
                            assertThat(pay.getPayStatus()).isNotEqualByComparingTo(PayStatus.PAID));
        }

        @Test
        @DisplayName("완료된 배송 -> 예외")
        void shouldFail_whenCompleted() {
            //given
            order.processPayment(pay);
            order.prepareDelivery(member.getAddr());
            order.startDelivery(trackingNo, shippedAt);

            //when
            order.completeDelivery(arrivedAt);

            //then
            assertThatThrownBy(() -> order.prepareDelivery(member.getAddr()))
                    .isInstanceOfSatisfying(DeliveryStatusException.class, e -> {
                        assertThat(e.getDomain()).isEqualTo(DomainType.DELIVERY);
                        assertThat(e.getCurrentStatus()).isEqualTo(DeliveryStatus.COMPLETED);
                        assertThat(e.getTargetStatus()).isEqualTo(DeliveryStatus.READY);
                    });
        }

    }

    @Nested
    @DisplayName("startDelivery()")
    class StartDelivery {
        @Test
        @DisplayName("정상 -> 배송 시작")
        void success() {
            //given
            order.processPayment(pay);
            order.prepareDelivery(member.getAddr());

            //when
            order.startDelivery(trackingNo, shippedAt);

            //then
            assertSoftly(softly -> {
                softly.assertThat(order.getDelivery().getDeliveryStatus()).isEqualTo(DeliveryStatus.SHIPPING);
                softly.assertThat(order.getDelivery().getShippedAt()).isNotNull();
                softly.assertThat(order.getDelivery().getArrivedAt()).isNull();
                softly.assertThat(order.getOrderStatus()).isEqualTo(OrderStatus.CONFIRMED);
            });
        }

        @Test
        @DisplayName("결제 전 -> 예외")
        void shouldFail_whenNotPaid() {
            assertThatThrownBy(() -> order.startDelivery(trackingNo, shippedAt))
                    .isInstanceOf(DeliveryException.class);
        }

        @Test
        @DisplayName("결제 후/준비되지 않은 배송 -> 예외")
        void shouldFail_whenNotPrepared() {
            //given
            order.processPayment(pay);

            //then
            assertThatThrownBy(() -> order.startDelivery(trackingNo, shippedAt))
                    .isInstanceOf(DeliveryException.class);
            assertThat(order.getPay().getPayStatus()).isEqualTo(PayStatus.PAID);
        }

        @Test
        @DisplayName("취소된 배송 -> 예외")
        void shouldFail_whenCanceled() {
            //given
            order.processPayment(pay);
            order.prepareDelivery(member.getAddr());

            //when
            order.cancel();

            //then
            assertThatThrownBy(() -> order.startDelivery(trackingNo,shippedAt))
                    .isInstanceOfSatisfying(DeliveryStatusException.class, e -> {
                        assertThat(e.getDomain()).isEqualTo(DomainType.DELIVERY);
                        assertThat(e.getCurrentStatus()).isEqualTo(DeliveryStatus.CANCELED);
                        assertThat(e.getTargetStatus()).isEqualTo(DeliveryStatus.SHIPPING);
                    });
        }

        @Test
        @DisplayName("완료된 배송 -> 예외")
        void shouldFail_whenCompleted() {
            //given
            order.processPayment(pay);
            order.prepareDelivery(member.getAddr());
            order.startDelivery(trackingNo, shippedAt);

            //when
            order.completeDelivery(arrivedAt);

            //then
            assertThatThrownBy(() -> order.startDelivery(trackingNo,shippedAt))
                    .isInstanceOfSatisfying(DeliveryStatusException.class, e -> {
                        assertThat(e.getDomain()).isEqualTo(DomainType.DELIVERY);
                        assertThat(e.getCurrentStatus()).isEqualTo(DeliveryStatus.COMPLETED);
                        assertThat(e.getTargetStatus()).isEqualTo(DeliveryStatus.SHIPPING);
                    });
        }

    }

    @Nested
    @DisplayName("completeDelivery()")
    class CompleteDelivery {
        @Test
        @DisplayName("정상 -> 배송 완료")
        void success() {
            //given
            order.processPayment(pay);
            order.prepareDelivery(member.getAddr());
            order.startDelivery(trackingNo, shippedAt);

            //when
            order.completeDelivery(arrivedAt);

            //then
            assertSoftly(softly -> {
                softly.assertThat(order.getOrderStatus()).isEqualTo(OrderStatus.COMPLETED);
                softly.assertThat(order.getDelivery().getDeliveryStatus()).isEqualTo(DeliveryStatus.COMPLETED);
                softly.assertThat(order.getDelivery().getArrivedAt()).isNotNull();
            });
        }

        @Test
        @DisplayName("결제 전 -> 예외")
        void shouldFail_whenNotPaid() {
            assertThatThrownBy(() -> order.completeDelivery(shippedAt))
                    .isInstanceOf(DeliveryException.class);
        }

        @Test
        @DisplayName("결제 후/준비되지 않은 배송 -> 예외")
        void shouldFail_whenNotPrepared() {
            //given
            order.processPayment(pay);

            //then
            assertThat(order.getPay().getPayStatus()).isEqualTo(PayStatus.PAID);
            assertThatThrownBy(() -> order.completeDelivery(shippedAt))
                    .isInstanceOf(DeliveryException.class);
        }

        @Test
        @DisplayName("취소된 배송 -> 예외")
        void shouldFail_whenCanceled() {
            //given
            order.processPayment(pay);
            order.prepareDelivery(member.getAddr());

            //when
            order.cancel();

            //then
            assertThatThrownBy(() -> order.completeDelivery(shippedAt))
                    .isInstanceOfSatisfying(DeliveryStatusException.class, e -> {
                        assertThat(e.getDomain()).isEqualTo(DomainType.DELIVERY);
                        assertThat(e.getCurrentStatus()).isEqualTo(DeliveryStatus.CANCELED);
                        assertThat(e.getTargetStatus()).isEqualTo(DeliveryStatus.COMPLETED);
                    });
        }

        @Test
        @DisplayName("완료된 배송 -> 예외")
        void shouldFail_whenCompleted() {
            //given
            order.processPayment(pay);
            order.prepareDelivery(member.getAddr());
            order.startDelivery(trackingNo, shippedAt);

            //when
            order.completeDelivery(arrivedAt);

            //then
            assertThatThrownBy(() -> order.completeDelivery(shippedAt))
                    .isInstanceOfSatisfying(DeliveryStatusException.class, e -> {
                        assertThat(e.getDomain()).isEqualTo(DomainType.DELIVERY);
                        assertThat(e.getCurrentStatus()).isEqualTo(DeliveryStatus.COMPLETED);
                        assertThat(e.getTargetStatus()).isEqualTo(DeliveryStatus.COMPLETED);
                    });
        }
    }
}
