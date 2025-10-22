package com.minimall.api.domain.order;

import com.minimall.api.domain.member.Grade;
import com.minimall.api.domain.member.Member;
import com.minimall.api.domain.member.MemberRepository;
import com.minimall.api.domain.order.exception.OrderStatusException;
import com.minimall.api.domain.order.sub.delivery.Delivery;
import com.minimall.api.domain.order.sub.delivery.DeliveryStatus;
import com.minimall.api.domain.order.sub.delivery.exception.DeliveryStatusException;
import com.minimall.api.domain.order.sub.pay.Pay;
import com.minimall.api.domain.order.sub.pay.PayMethod;
import com.minimall.api.domain.order.sub.pay.PayStatus;
import com.minimall.api.domain.product.Product;
import com.minimall.api.domain.embeddable.Address;
import com.minimall.api.domain.embeddable.AddressException;
import com.minimall.api.domain.product.ProductRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
public class OrderWithSubDomainTest {

    @Autowired
    MemberRepository memberRepository;

    @Autowired
    ProductRepository productRepository;

    @Autowired
    OrderRepository orderRepository;

    @Test
    @DisplayName("멤버, 상품, 주문 매핑 후 저장/조회")
    void orderBasicTest() {
        //given
        Order order = createOrder();
        orderRepository.save(order);

        //when
        Order findOrder = orderRepository.findById(order.getId()).get();

        //then
        assertThat(findOrder.getOrderStatus()).isEqualTo(OrderStatus.ORDERED);
        assertThat(findOrder.getOrderAmount().getOriginalAmount()).isEqualTo(2000000);
        assertThat(findOrder.getMember().getName()).isEqualTo("차태승");
        assertThat(findOrder.getMember().getGrade()).isEqualTo(Grade.BRONZE);
        assertThat(findOrder.getOrderItems().size()).isEqualTo(2);
        assertThat(findOrder.getOrderItems().getFirst().getProductName()).isEqualTo("키보드");
        assertThat(findOrder.getOrderItems().get(1).getProductName()).isEqualTo("무선마우스");
    }

    @Test
    @DisplayName("결제 동작")
    void payBasicTest() {
        //given
        Order order = createOrder();
        Pay pay = new Pay(PayMethod.CARD);

        //then
        assertThat(pay.getPayStatus()).isEqualTo(PayStatus.READY);
        assertThat(pay.getPayMethod()).isEqualTo(PayMethod.CARD);
    }


    @Test
    void deliveryBasicTest() {
        //given
        Order order = createOrder();

        //결제 되지 않은 주문은 배송 준비 불가
        assertThrows(OrderStatusException.class, () -> order.prepareDelivery(createAddress()));

        order.processPayment(new Pay(PayMethod.CARD));

        //주소 없이 배송 준비 불가
        assertThrows(AddressException.class, () -> order.prepareDelivery(null));

        order.prepareDelivery(createAddress());
        assertThat(order.getDelivery().getDeliveryStatus()).isEqualTo(DeliveryStatus.READY);

        //배송중이지 않은 배송은 완료 불가
        assertThrows(DeliveryStatusException.class, order::completeDelivery);

        order.startDelivery();
        assertThat(order.getDelivery().getDeliveryStatus()).isEqualTo(DeliveryStatus.SHIPPING);

        //준비 상태가 아닌 배송은 배송 시작 불가
        assertThrows(DeliveryStatusException.class, order::startDelivery);

        order.completeDelivery();
        assertThat(order.getDelivery().getDeliveryStatus()).isEqualTo(DeliveryStatus.COMPLETED);

    }

    @Test
    @DisplayName("재고보다 많은 양을 주문하면 예외 발생")
    void shouldFail_whenOrderQuantityGreaterThanProductStockQuantity() {
        //given
        Product product = new Product("키보드", 100000, 10);
        productRepository.save(product);

        //when, then
        assertThrows(IllegalArgumentException.class, () -> OrderItem.createOrderItem(product, 20));
    }


    @Test
    void orderPay() {
        //given
        Order order = createOrder();
        Pay pay = new Pay(PayMethod.BANK_TRANSFER);

        //when
        order.processPayment(pay);

        //then
        assertThat(order.getPay().getPayStatus()).isEqualTo(PayStatus.PAID);
        assertThat(order.getPay().getPayMethod()).isEqualTo(PayMethod.BANK_TRANSFER);
        assertThat(order.getOrderStatus()).isEqualTo(OrderStatus.CONFIRMED);
    }

    @Test
    void prepareDelivery_shouldFail_whenPayStatusIsNotPaid() {
        //given
        Order order = createOrder();
        Pay pay = new Pay(PayMethod.MOBILE_PAY);
        order.processPayment(pay);
    }

    @Test
    void orderCancel() {
        //given
        Order order = createOrder();

        //when
        order.cancel();

        //then
        assertThat(order.getOrderStatus()).isEqualTo(OrderStatus.CANCELED);
        assertThrows(OrderStatusException.class, order::cancel);
    }

    @Test
    void orderAndPayCancel() {
        //given
        Order order = createOrder();
        Pay pay = new Pay(PayMethod.CARD);
        order.processPayment(pay);

        //when
        order.cancel();

        //then
        assertThat(order.getOrderStatus()).isEqualTo(OrderStatus.CANCELED);
        assertThat(order.getPay().getPayStatus()).isEqualTo(PayStatus.CANCELED);
    }

    @Test
    void orderAndDeliveryCancel() {
        //given
        Order order = createOrder();
        Delivery.readyDelivery(order, createAddress());

        //when
        order.cancel();

        //then
        assertThat(order.getOrderStatus()).isEqualTo(OrderStatus.CANCELED);
        assertThat(order.getDelivery().getDeliveryStatus()).isEqualTo(DeliveryStatus.CANCELED);
    }

    @Test
    void cancel_shouldFail_whenDeliveryAlreadyStarted() {
        //given
        Order order = createOrder();
        Delivery.readyDelivery(order, createAddress());

        //when
        order.startDelivery();

        //then
        assertThrows(OrderStatusException.class, order::cancel);
    }

    @Test
    void completeDelivery_shouldFail_whenDeliveryStatusNotShipping() {
        //given
        Order order = createOrder();
        Delivery.readyDelivery(order, createAddress());

        //when, then
        assertThrows(DeliveryStatusException.class, order::completeDelivery);
    }

    @Test
    void cancel_shouldFail_whenDeliveryAlreadyCompleted() {
        //given
        Order order = createOrder();
        Delivery.readyDelivery(order, createAddress());

        //when
        order.startDelivery();
        order.completeDelivery();

        //then
        assertThrows(OrderStatusException.class, order::cancel);
    }


    //==Helper Methods==//
    private Order createOrder() {
        Member member = createMember();
        List<OrderItem> orderItems = createOrderItems();
        return Order.createOrder(member, orderItems.getFirst(), orderItems.get(1));
    }

    private Member createMember() {
        return memberRepository.save(Member.builder()
                .loginId("user1")
                .password("abc12345")
                .name("차태승")
                .email("cts9458@naver.com")
                .build());
    }

    private List<OrderItem> createOrderItems() {
        List<OrderItem> orderItems = new ArrayList<>();

        Product product1 = new Product("키보드", 100000, 10);
        Product product2 = new Product("무선마우스", 50000, 50);

        productRepository.save(product1);
        productRepository.save(product2);

        orderItems.add(OrderItem.createOrderItem(product1, 10));
        orderItems.add(OrderItem.createOrderItem(product2, 20));

        return orderItems;
    }

    private Address createAddress() {
        return Address.builder()
                .postcode("62350")
                .state("광주광역시")
                .city("광산구")
                .street("수등로76번길 10")
                .detail("수완대방노블랜드아파트 123동 1540호")
                .build();
    }
}
