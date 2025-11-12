package com.minimall.domain.order;

import com.minimall.domain.common.base.BaseEntity;
import com.minimall.domain.embeddable.Address;
import com.minimall.domain.embeddable.InvalidAddressException;
import com.minimall.domain.member.Member;
import com.minimall.domain.order.delivery.DeliveryStatus;
import com.minimall.domain.order.delivery.DeliveryStatusException;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.lang.Nullable;

import java.time.LocalDateTime;
import java.util.Optional;

import static com.minimall.domain.order.delivery.DeliveryStatus.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Delivery extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "delivery_id")
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    private Order order;

    @Enumerated(EnumType.STRING)
    private DeliveryStatus deliveryStatus;

    @Setter
    private String trackingNo;

    @Embedded
    private Address shipAddr;

    private Integer deliveryFee; //TODO DB 컬럼 추가

    private LocalDateTime shippedAt;     //TODO DB 컬럼 추가
    private LocalDateTime arrivedAt;     //TODO DB 컬럼 추가



    //==생성자==//
    public static Delivery readyDelivery(Order order, Address shipAddr) {
        //TODO Order:Delivery -> 1:N 생각(배송 취소, 실패 시 새로운 배송 추가)
        if (order.getDelivery() != null) {
            Delivery delivery = order.getDelivery();
            throw new DeliveryStatusException(delivery.getId(), delivery.getDeliveryStatus(), READY);
        }
        return new Delivery(order, shipAddr);
    }

    private Delivery(Order order, Address shipAddr) {
        this.order = order;
        this.shipAddr = shipAddr;
        deliveryStatus = READY;
    }

    //== 연관관계 메서드 ==//
    void assignOrder(Order order) {
        this.order = order;
    }

    //== 비즈니스 로직 ==//
    void startDelivery(String trackingNo, LocalDateTime shippedAt) {
        ensureCanTransition(SHIPPING);
        deliveryStatus = SHIPPING;
        this.trackingNo = trackingNo;
        this.shippedAt = shippedAt;
    }

    void completeDelivery(LocalDateTime arrivedAt) {
        ensureCanTransition(COMPLETED);
        deliveryStatus = COMPLETED;
        this.arrivedAt = arrivedAt;
    }

    void cancel() {
        ensureCanTransition(CANCELED);
        deliveryStatus = CANCELED;
    }

    //== 검증 로직 ==//
    private void ensureCanTransition(DeliveryStatus next) {
        if (!deliveryStatus.canProgressTo(next)) {
            throw new DeliveryStatusException(id, deliveryStatus, next);
        }
    }
}

