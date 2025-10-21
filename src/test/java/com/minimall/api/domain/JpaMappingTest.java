package com.minimall.api.domain;

import com.minimall.api.domain.member.Grade;
import com.minimall.api.domain.member.Member;
import com.minimall.api.domain.member.MemberRepository;
import com.minimall.api.domain.order.Order;
import com.minimall.api.domain.order.OrderItem;
import com.minimall.api.domain.order.OrderRepository;
import com.minimall.api.domain.order.OrderStatus;
import com.minimall.api.domain.order.exception.OrderAlreadyCanceledException;
import com.minimall.api.domain.order.sub.delivery.Delivery;
import com.minimall.api.domain.order.sub.delivery.DeliveryStatus;
import com.minimall.api.domain.order.sub.delivery.exception.DeliveryStatusException;
import com.minimall.api.domain.order.sub.pay.Pay;
import com.minimall.api.domain.order.sub.pay.PayMethod;
import com.minimall.api.domain.order.sub.pay.PayStatus;
import com.minimall.api.domain.product.Product;
import com.minimall.api.domain.product.ProductRepository;
import com.minimall.api.embeddable.Address;
import com.minimall.api.embeddable.AddressException;
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
@Transactional
public class JpaMappingTest {

    @Autowired
    MemberRepository memberRepository;

    @Autowired
    OrderRepository orderRepository;

    @Autowired
    ProductRepository productRepository;

    @Test
    @DisplayName("멤버 저장/조회")
    void memberBasicTest() {
        //given
        Member member = createMember();

        //when
        memberRepository.save(member);
        Member findMember = memberRepository.findById(member.getId()).get();

        //then
        assertThat(findMember.getName()).isEqualTo("차태승");
        assertThat(findMember.getGrade()).isEqualTo(Grade.BRONZE); // default Grade: BRONZE
    }


    @Test
    @DisplayName("상품 저장/조회")
    void productBasicTest() {
        //given
        Product product1 = new Product("노트북", 1500000, 10);
        Product product2 = new Product("무선마우스", 35000, 50);
        Product product3 = new Product("기계식키보드", 120000, 30);

        //when
        productRepository.save(product1);
        productRepository.save(product2);
        productRepository.save(product3);
        List<Product> productList = productRepository.findAll();
        Product findProduct1 = productRepository.findById(product1.getId()).get();
        Product findProduct2 = productRepository.findById(product2.getId()).get();

        //then
        assertThat(productList.size()).isEqualTo(3);
        assertThat(findProduct1.getName()).isEqualTo("노트북");
        assertThat(findProduct1).isEqualTo(product1);
        assertThat(findProduct2).isEqualTo(product2);
    }

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

        //결제되지 않은 상태에서 배송 준비 불가
        assertThrows(IllegalStateException.class, () -> order.prepareDelivery(createAddress()));

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
    void productStockTest() {
        //given
        Product product = new Product("노트북", 1500000, 10);
        productRepository.save(product);

        //when
        product.addStock(20);
        Product findProduct = productRepository.findById(product.getId()).get();

        //then
        assertThat(findProduct.getStockQuantity()).isEqualTo(30);
        assertThrows(IllegalArgumentException.class, () -> product.removeStock(10000)); // 재고 과다 감량 -> 오류 발생
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
        assertThat(order.getOrderStatus()).isEqualTo(OrderStatus.COMPLETED);
    }

    @Test
    void orderCancel() {
        //given
        Order order = createOrder();

        //when
        order.cancel();

        //then
        assertThat(order.getOrderStatus()).isEqualTo(OrderStatus.CANCELED);
        assertThrows(OrderAlreadyCanceledException.class, order::cancel);
    }

    @Test
    void orderAndPayCancel() {
        //given
        Order order = createOrder();
        order.processPayment(new Pay(PayMethod.CARD));

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
        Delivery.createDelivery(order, createAddress());

        //when
        order.cancel();

        //then
        assertThat(order.getOrderStatus()).isEqualTo(OrderStatus.CANCELED);
        assertThat(order.getDelivery().getDeliveryStatus()).isEqualTo(DeliveryStatus.CANCELED);
    }


    //==helper method==//
    private Order createOrder() {
        Member member = createMember();
        List<OrderItem> orderItems = createOrderItems();
        return Order.createOrder(member, orderItems.getFirst(), orderItems.get(1));
    }

    private Member createMember() {
        return Member.builder()
                .loginId("user1")
                .password("abc12345")
                .name("차태승")
                .email("cts9458@naver.com")
                .build();
    }

    private List<OrderItem> createOrderItems() {
        List<OrderItem> orderItems = new ArrayList<>();

        Product product1 = new Product("키보드", 100000, 10);
        Product product2 = new Product("무선마우스", 50000, 50);

        orderItems.add(createOrderItem(product1, 10));
        orderItems.add(createOrderItem(product2, 20));

        return orderItems;
    }

    private OrderItem createOrderItem(Product product, int orderQuantity) {
        return OrderItem.builder()
                .product(product)
                .productName(product.getName())
                .orderPrice(product.getPrice())
                .orderQuantity(orderQuantity)
                .build();
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
