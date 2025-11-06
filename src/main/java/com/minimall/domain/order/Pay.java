package com.minimall.domain.order;

import com.minimall.domain.common.base.BaseEntity;
import com.minimall.domain.order.pay.PayAmountMismatchException;
import com.minimall.domain.order.pay.PayStatusException;
import com.minimall.domain.order.pay.PayMethod;
import com.minimall.domain.order.pay.PayStatus;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Pay extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
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


    //== 생성자 ==//
    public Pay(PayMethod payMethod, int payAmount) {
        this.payMethod = payMethod;
        this.payAmount = payAmount;
        payStatus = PayStatus.READY;
    }


    //== 연관관계 메서드 ==//
    void assignOrder(Order order) {
        this.order = order;
    }


    //== 비즈니스 로직 ==//
    void complete() {
        ensureCanTransition(PayStatus.PAID);
        payStatus = PayStatus.PAID;
        paidAt = LocalDateTime.now();
    }

    void cancel() {
        ensureCanTransition(PayStatus.CANCELED);
        payStatus = PayStatus.CANCELED;
    }

    void fail() {
        ensureCanTransition(PayStatus.FAILED);
        payStatus = PayStatus.FAILED;
    }


    //== 검증 로직 ==//
    private void ensureCanTransition(PayStatus next) {
        if (!payStatus.canProgressTo(next)) {
            throw new PayStatusException(id, payStatus, next);
        }
    }

    public void validateAmount(int expectedAmount) {
        if (payAmount != expectedAmount) {
            throw new PayAmountMismatchException(order.getId(), expectedAmount, payAmount);
        }
    }
}
