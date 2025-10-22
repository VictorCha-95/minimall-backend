package com.minimall.api.domain.order.sub.delivery;

import com.minimall.api.common.base.BaseEntity;
import com.minimall.api.domain.order.Order;
import com.minimall.api.domain.order.sub.delivery.exception.DeliveryStatusException;
import com.minimall.api.domain.embeddable.Address;
import com.minimall.api.domain.embeddable.AddressException;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import static com.minimall.api.domain.order.sub.delivery.DeliveryStatus.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Delivery extends BaseEntity {

    @Id @GeneratedValue
    @Column(name = "delivery_id")
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    private Order order;

    @Enumerated(EnumType.STRING)
    private DeliveryStatus deliveryStatus;

    private String trackingNo;

    @Embedded
    private Address shipAddr;

    private Integer deliveryFee; //TODO DB 컬럼 추가



    //==생성자==//
    public static void readyDelivery(Order order, Address shipAddr) {
        validateShipAddr(shipAddr);
        Delivery delivery = new Delivery(order, shipAddr);
        order.setDelivery(delivery);
        delivery.prepareToShip();
    }

    private Delivery(Order order, Address shipAddr) {
        this.order = order;
        this.shipAddr = shipAddr;
    }

    //==연관관계 편의 메서드==//
    public void setOrder(Order order) {
        this.order = order;
        if (order.getDelivery() != this) {
            order.setDelivery(this);
        }
    }
    private void prepareToShip() {
        deliveryStatus = READY;
    }


    //==비즈니스 로직==//
    public void startDelivery() {
        ensureCanTransition(SHIPPING);
        deliveryStatus = SHIPPING;
    }
    public void completeDelivery() {
        ensureCanTransition(COMPLETED);
        deliveryStatus = COMPLETED;
    }

    public void cancel() {
        ensureCanTransition(CANCELED);
        deliveryStatus = CANCELED;
    }


    //==검증 로직==//
    private void ensureCanTransition(DeliveryStatus next) {
        if (!deliveryStatus.canProgressTo(next)) {
            throw new DeliveryStatusException(id, deliveryStatus, next);
        }
    }

    private static void validateShipAddr(Address shipAddr) {
        if (shipAddr == null ||
            shipAddr.getPostcode() == null ||
            shipAddr.getState() == null ||
            shipAddr.getCity() == null ||
            shipAddr.getStreet() == null) {
            throw new AddressException("배송 주소의 필수 항목이 누락되었습니다");
        }
    }
}
