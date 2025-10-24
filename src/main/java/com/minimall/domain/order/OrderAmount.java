package com.minimall.domain.order;

import com.minimall.domain.member.Member;
import com.minimall.domain.order.sub.discount.MemberDiscountPolicy;
import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OrderAmount {

    private Integer originalAmount;        // 할인 적용 전 금액
    private Integer discountAmount;
    private Integer finalAmount;        // 할인 적용 후 금액
    private boolean isDiscounted = false;

    //==생성자==//
    public OrderAmount(int originalAmount) {
        this.originalAmount = originalAmount;
        discountAmount = 0;
        finalAmount = originalAmount;
    }


    //==비즈니스 로직==//
    public void applyDiscount(Member member, MemberDiscountPolicy discountPolicy, int extraDiscount) {
        checkAlreadyDiscounted();
        applyMemberDiscount(member, discountPolicy);
        applyAdditionalDiscount(extraDiscount);
        finalizeDiscount();
    }

    public void applyDiscount(Member member, MemberDiscountPolicy discountPolicy) {
        checkAlreadyDiscounted();
        applyMemberDiscount(member, discountPolicy);
        finalizeDiscount();
    }

    private void applyMemberDiscount(Member member, MemberDiscountPolicy discountPolicy) {
        int discount = discountPolicy.discount(member, originalAmount);
        discountAmount += discount;
    }

    private void applyAdditionalDiscount(int extraDiscount) {
        if (extraDiscount < 0) {
            throw new IllegalArgumentException("추가 할인 금액은 음수일 수 없습니다. 입력값: " + extraDiscount);
        }
        discountAmount += extraDiscount;
    }


    //==예외 로직==//
    private void discountAmountValidate() {
        if (finalAmount < 0) {
            throw new IllegalArgumentException("할인 금액이 총 금액 보다 많습니다. " +
                    "총 금액: " + originalAmount + ", 할인 금액: " + discountAmount);
        }
    }

    private void checkAlreadyDiscounted() {
        if (isDiscounted) {
            throw new IllegalStateException("이미 할인이 적용된 주문서입니다.");
        }
    }


    //==공통 로직==//
    private void finalizeDiscount() {
        finalAmount = originalAmount - discountAmount;
        discountAmountValidate();
        isDiscounted = true;
    }
}