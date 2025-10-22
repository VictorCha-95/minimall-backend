package com.minimall.api.domain.order;

import com.minimall.api.common.base.BaseEntity;
import com.minimall.api.domain.member.Member;
import com.minimall.api.domain.order.exception.OrderStatusException;
import com.minimall.api.domain.order.sub.delivery.Delivery;
import com.minimall.api.domain.order.sub.pay.Pay;
import com.minimall.api.domain.embeddable.Address;
import com.minimall.api.domain.embeddable.AddressException;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Entity
@Table(name = "orders")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Order extends BaseEntity {

    @Id @GeneratedValue
    @Column(name = "order_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    private LocalDateTime orderedAt;

    @Enumerated(EnumType.STRING)
    private OrderStatus orderStatus;

    @Embedded
    private OrderAmount orderAmount;

    @Embedded
    private Address shipAddr;  // 역정규화(주문 시 배송지 입력 default member.getAddr() //TODO DB 컬럼 추가

    @OneToOne(fetch = FetchType.LAZY, mappedBy = "order")
    private Delivery delivery;

    @OneToOne(fetch = FetchType.LAZY, mappedBy = "order")
    private Pay pay;

    @OneToMany(mappedBy = "order")
    private List<OrderItem> orderItems = new ArrayList<>();


    //==생성자==//
    public static Order createOrder(Member member, OrderItem... items) {
        //주문 항목 토탈 금액(할인 금액은 Pay 처리 과정에서 적용)
        int originalTotalAmount = Arrays.stream(items)
                .mapToInt(oi -> oi.getOrderPrice() * oi.getOrderQuantity())
                .sum();

        //order 생성
        Order order = Order.builder()
                .member(member)
                .orderedAt(LocalDateTime.now())
                .orderStatus(OrderStatus.ORDERED)
                .orderAmount(new OrderAmount(originalTotalAmount))
                .build();

        for (OrderItem item : items) {
            order.addOrderItem(item);
        }

        return order;
    }

    @Builder(access = AccessLevel.PRIVATE)
    private Order(Member member, LocalDateTime orderedAt, OrderStatus orderStatus, OrderAmount orderAmount) {
        this.member = member;
        this.orderedAt = orderedAt;
        this.orderStatus = orderStatus;
        this.orderAmount = orderAmount;
    }


    //==연관관계 메서드==//
    public void setMember(Member member) {
        this.member = member;
        if (!member.getOrders().contains(this)) {
            member.getOrders().add(this);
        }
    }

    public void addOrderItem(OrderItem orderItem) {
        orderItems.add(orderItem);
        if (orderItem.getOrder() != this) {
            orderItem.setOrder(this);
        }
    }

    public void setDelivery(Delivery delivery) {
        this.delivery = delivery;
        if (delivery.getOrder() != this) {
            delivery.setOrder(this);
        }
    }

    public void setPay(Pay pay) {
        this.pay = pay;
        if (pay.getOrder() != this) {
            pay.setOrder(this);
        }
    }


    //==비즈니스 로직==//
    public void processPayment(Pay pay) {
        ensureCanTransition(OrderStatus.CONFIRMED);
        setPay(pay);
        pay.completePayment(orderAmount.getFinalAmount());
        orderStatus = OrderStatus.CONFIRMED;
    }

    public void prepareDelivery(Address shipAddr) {
        ensureCanTransition(OrderStatus.SHIPPING);

        if (shipAddr == null) {
            throw new AddressException("배송 주소는 필수");
        }

        Delivery.readyDelivery(this, shipAddr);
    }

    public void startDelivery() {
        delivery.startDelivery();
        orderStatus = OrderStatus.SHIPPING;
    }

    public void completeDelivery() {
        delivery.completeDelivery();
        orderStatus = OrderStatus.COMPLETED;
    }

    public void cancel() {   // 배송 시작 이전 단계에서만 취소 가능
        ensureCanTransition(OrderStatus.CANCELED);
        if (pay != null) pay.cancel();
        if (delivery != null) delivery.cancel();
        orderItems.forEach(oi -> oi.getProduct().addStock(oi.getOrderQuantity())); //주문 취소 후 재고 복원
        orderStatus = OrderStatus.CANCELED;
    }


    //==검증 로직==//
    private void ensureCanTransition(OrderStatus next) {
        if (!orderStatus.canProgressTo(next)) {
            throw new OrderStatusException(id, orderStatus, next);
        }
    }
}
