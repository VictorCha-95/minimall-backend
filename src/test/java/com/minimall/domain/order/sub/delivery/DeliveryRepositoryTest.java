package com.minimall.domain.order.sub.delivery;

import com.minimall.domain.embeddable.Address;
import com.minimall.domain.member.Member;
import com.minimall.domain.member.MemberRepository;
import com.minimall.domain.order.Order;
import com.minimall.domain.order.OrderItem;
import com.minimall.domain.order.OrderRepository;
import com.minimall.domain.product.Product;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

@DataJpaTest
class DeliveryRepositoryTest {

    @Autowired
    MemberRepository memberRepository;

    @Autowired
    OrderRepository orderRepository;

    @Autowired
    DeliveryRepository deliveryRepository;

    @BeforeEach
    void before() {
        Delivery delivery = Delivery.readyDelivery(createOrder(), createAddress());
        delivery.setTrackingNo("12345-12345");
        deliveryRepository.save(delivery);
    }

    @Test
    void findByTrackingNo() {
        //when
        Delivery found = deliveryRepository.findByTrackingNo("12345-12345")
                .orElseThrow(() -> new IllegalArgumentException("배송정보 없음"));

        //then
        assertThat(found.getTrackingNo()).isEqualTo("12345-12345");
    }

    @Test
    void findByDeliveryStatus() {
        //when
        List<Order> orders = orderRepository.findAll();
        orders.getFirst().startDelivery();
        List<Delivery> found = deliveryRepository.findByDeliveryStatus(DeliveryStatus.SHIPPING);

        //then
        assertThat(found)
                .extracting(Delivery::getTrackingNo)
                .containsExactly("12345-12345");
    }


    //==Helper Methods==//
    private Order createOrder() {
        Member member = createMember();
        List<OrderItem> orderItems = createOrderItems();
        return orderRepository.save(Order.createOrder(member, orderItems.getFirst(), orderItems.get(1)));
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