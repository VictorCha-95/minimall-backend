package com.minimall.domain.order;

import com.minimall.domain.member.Member;
import com.minimall.domain.member.MemberRepository;
import com.minimall.domain.order.sub.pay.PayRepository;
import com.minimall.domain.product.Product;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

@DataJpaTest
class OrderRepositoryTest {

    @Autowired
    MemberRepository memberRepository;

    @Autowired
    OrderRepository orderRepository;

    @Autowired
    PayRepository payRepository;

    @BeforeEach
    void before() {
        createOrder();
    }

    @Test
    void findByMember() {
        //when
        Member foundMember = memberRepository.findByName("차태승").getFirst();
        List<Order> foundOrder = orderRepository.findByMember(foundMember);

        //then
        assertThat(foundOrder)
                .extracting(Order::getMember)
                .containsExactly(foundMember);

        assertThat(foundOrder)
                .extracting(Order::getMember)
                .extracting(Member::getName)
                .containsExactly("차태승");
    }

    @Test
    void findByMemberAndOrderStatus() {
        //given
        Member foundMember = memberRepository.findByName("차태승").getFirst();

        //when
        List<Order> foundOrder = orderRepository.findByMemberAndOrderStatus(foundMember, OrderStatus.ORDERED);
        List<Order> notFoundOrder = orderRepository.findByMemberAndOrderStatus(foundMember, OrderStatus.SHIPPING);

        //then
        assertThat(foundOrder)
                .extracting(Order::getOrderStatus)
                .containsExactly(OrderStatus.ORDERED);

        assertThat(foundOrder)
                .extracting(Order::getMember)
                .containsExactly(foundMember);

        assertThat(notFoundOrder).isEmpty();
    }

    //==Helper Methods==//
    private void createOrder() {
        Member member = createMember();
        List<OrderItem> orderItems = createOrderItems();
        orderRepository.save(Order.createOrder(member, orderItems.getFirst(), orderItems.get(1)));
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
}