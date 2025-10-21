package com.minimall.api.domain.order.sub.pay;

import com.minimall.api.common.base.BaseEntity;
import com.minimall.api.domain.order.Order;
import com.minimall.api.domain.order.sub.pay.exception.PayAmountMismatchException;
import com.minimall.api.domain.order.sub.pay.exception.PayStatusException;
import com.minimall.api.domain.order.sub.pay.exception.NotPaidException;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Pay extends BaseEntity {

    @Id @GeneratedValue
    @Column(name = "pay_id")
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    private Order order;

    @Enumerated(EnumType.STRING)
    private PayMethod payMethod;

    private Integer payAmount;

    @Enumerated(EnumType.STRING)
    private PayStatus payStatus;

    private LocalDateTime paidAt;


    //==생성자==//
    public Pay(PayMethod payMethod) {
        this.payMethod = payMethod;
        payStatus = PayStatus.READY;
    }



    //==연관관계 메서드==//
    public void setOrder(Order order) {
        this.order = order;
        payAmount = order.getOrderAmount().getFinalAmount();
        if (order.getPay() != this) {
            order.setPay(this);
        }
    }

    //==비즈니스 로직==//
    public void completePayment() {
        payStatus = PayStatus.PAID;
        paidAt = LocalDateTime.now();
    }

    public void cancel() {
        ensurePaid();
        payStatus = PayStatus.CANCELED;
    }


    //==검증 로직==//
    public void validateAmount(int orderAmount) {
        if (payAmount != orderAmount) {
            payStatus = PayStatus.FAILED;
            throw new PayAmountMismatchException(order.getId(), orderAmount, payAmount);
        }
    }

    public void ensurePaid() {
        if (payStatus != PayStatus.PAID) {
            throw new NotPaidException(order.getId(), payStatus);
        }
    }
}
