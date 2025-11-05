package com.minimall.domain.order;

import com.minimall.domain.common.base.BaseEntity;
import com.minimall.domain.member.Member;
import com.minimall.domain.embeddable.Address;
import com.minimall.domain.embeddable.InvalidAddressException;
import com.minimall.domain.order.exception.InvalidOrderItemException;
import com.minimall.domain.order.exception.OrderStatusException;
import com.minimall.domain.order.exception.PaymentRequiredException;
import com.minimall.domain.order.pay.PayAmountMismatchException;
import com.minimall.domain.order.pay.PayStatus;
import com.minimall.domain.order.status.OrderStatus;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.*;

@Entity
@Table(name = "orders")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Order extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Column(nullable = false)
    private LocalDateTime orderedAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus orderStatus;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "originalAmount", column = @Column(name = "original_amount", nullable = false)),
            @AttributeOverride(name = "discountAmount", column = @Column(name = "discount_amount", nullable = false)),
            @AttributeOverride(name = "finalAmount",    column = @Column(name = "final_amount", nullable = false))
    })
    private OrderAmount orderAmount;

    @OneToOne(fetch = FetchType.LAZY, mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private Delivery delivery;

    @OneToOne(fetch = FetchType.LAZY, mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private Pay pay;

    @OneToMany(mappedBy = "order", cascade = CascadeType.PERSIST, orphanRemoval = true)
    private List<OrderItem> orderItems = new ArrayList<>();


    //==생성자==//
    public static Order createOrder(Member member, OrderItem... items) {
        Objects.requireNonNull(member, OrderMessage.MEMBER_REQUIRED_FOR_ORDER_CREATION.text());

        validateOrderItems(items);

        Order order = new Order(member, LocalDateTime.now(),
                OrderStatus.ORDERED, new OrderAmount(getTotalAmount(items)));

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

    private static void validateOrderItems(OrderItem[] items) {
        if (items == null || items.length == 0) {
            throw InvalidOrderItemException.require();
        }

        for (int i = 0; i < items.length; i++) {
            OrderItem item = items[i];
            if (item == null) {
                throw InvalidOrderItemException.nullItemAt(i);
            }
        }
    }

    private static int getTotalAmount(OrderItem[] items) {
        return Arrays.stream(items)
                .mapToInt(OrderItem::getTotalAmount)
                .sum();
    }


    //==연관관계 메서드==//
    private void setMember(Member member) {
        this.member = member;
        if (!member.getOrders().contains(this)) {
            member.addOrder(this);
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
    public void cancel() {   // 배송 시작 이전 단계에서만 취소 가능
        ensureCanTransition(OrderStatus.CANCELED);
        if (pay != null) pay.cancel();
        if (delivery != null) delivery.cancel();
        orderItems.forEach(oi -> oi.getProduct().addStock(oi.getOrderQuantity())); //주문 취소 후 재고 복원
        orderStatus = OrderStatus.CANCELED;
    }

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
        ensurePaidForDelivery();
        ensureCanTransition(OrderStatus.SHIP_READY);
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

    public List<OrderItem> getOrderItems() {
        return Collections.unmodifiableList(orderItems);
    }


    //==검증 로직==//
    private void ensurePaidForDelivery() {
        if (pay == null) {
            throw PaymentRequiredException.mustBePaidBeforeDelivery(id);
        }
        if (pay.getPayStatus() != PayStatus.PAID) {
            throw PaymentRequiredException.mustBePaidBeforeDelivery(id, pay.getPayStatus());
        }
    }

    private void ensureCanTransition(OrderStatus next) {
        if (!orderStatus.canProgressTo(next)) {
            throw new OrderStatusException(id, orderStatus, next);
        }
    }

    private void validateShipAddr(Address shipAddr) {
        //TODO 이메일 형식 검증 추가
        if (shipAddr == null) shipAddr = member.getAddr();
        if (shipAddr == null) throw InvalidAddressException.required();
    }
}