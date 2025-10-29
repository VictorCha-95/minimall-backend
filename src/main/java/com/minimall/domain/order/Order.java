package com.minimall.domain.order;

import com.minimall.domain.common.base.BaseEntity;
import com.minimall.domain.member.Member;
import com.minimall.domain.embeddable.Address;
import com.minimall.domain.embeddable.InvalidAddressException;
import com.minimall.domain.order.exception.OrderStatusException;
import com.minimall.domain.order.exception.PayAmountMismatchException;
import jakarta.persistence.*;
import lombok.AccessLevel;
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

    @OneToOne(fetch = FetchType.LAZY, mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private Delivery delivery;

    @OneToOne(fetch = FetchType.LAZY, mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private Pay pay;

    @OneToMany(mappedBy = "order", cascade = CascadeType.PERSIST, orphanRemoval = true)
    private List<OrderItem> orderItems = new ArrayList<>();


    //==생성자==//
    public static Order createOrder(Member member, OrderItem... items) {
        //주문 항목 토탈 금액(할인 금액은 Pay 처리 과정에서 적용)
        int originalTotalAmount = Arrays.stream(items)
                .mapToInt(OrderItem::createTotalAmount)
                .sum();

        //order 생성
        Order order = new Order(member, LocalDateTime.now(), OrderStatus.ORDERED, new OrderAmount(originalTotalAmount));
        order.setMember(member);

        for (OrderItem item : items) {
            order.addOrderItem(item);
        }

        return order;
    }

    private Order(Member member, LocalDateTime orderedAt, OrderStatus orderStatus, OrderAmount orderAmount) {
        this.member = member;
        this.orderedAt = orderedAt;
        this.orderStatus = orderStatus;
        this.orderAmount = orderAmount;
    }


    //==연관관계 메서드==//
    private void setMember(Member member) {
        this.member = member;
        if (!member.getOrders().contains(this)) {
            member.getOrders().add(this);
        }
    }

    private void addOrderItem(OrderItem orderItem) {
        orderItems.add(orderItem);
        orderItem.assignOrder(this);
    }

    private void setDelivery(Delivery delivery) {
        this.delivery = delivery;
        delivery.assignOrder(this);
    }

    private void setPay(Pay pay) {
        this.pay = pay;
        pay.assignOrder(this);
    }


    //==비즈니스 로직==//
    public void processPayment(Pay pay) {
        ensureCanTransition(OrderStatus.CONFIRMED);
        setPay(pay);
        completePay(pay);
        orderStatus = OrderStatus.CONFIRMED;
    }

    private void completePay(Pay pay) {
        try {
            pay.validateAmount(getOrderAmount().getFinalAmount());
            pay.complete();
        } catch (PayAmountMismatchException e) {
            pay.fail();
            throw e;
        }
    }

    public void prepareDelivery(Address shipAddr) {
        ensureCanTransition(OrderStatus.SHIPPING);

        validateShipAddr(shipAddr);

        Delivery delivery = Delivery.readyDelivery(this, shipAddr);
        setDelivery(delivery);
        orderStatus = OrderStatus.SHIP_READY;
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
        orderItems.forEach(oi -> oi.getProduct().increaseStock(oi.getOrderQuantity())); //주문 취소 후 재고 복원
        orderStatus = OrderStatus.CANCELED;
    }


    //==검증 로직==//
    private void ensureCanTransition(OrderStatus next) {
        if (!orderStatus.canProgressTo(next)) {
            throw new OrderStatusException(id, orderStatus, next);
        }
    }
    private void validateShipAddr(Address shipAddr) {
        //TODO 이메일 형식 검증 추가
        if (shipAddr == null) shipAddr = member.getAddr();
        if (shipAddr == null) throw InvalidAddressException.empty();
    }
}