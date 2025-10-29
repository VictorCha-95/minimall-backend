package com.minimall.domain.order;

import com.minimall.domain.member.Member;
import com.minimall.domain.order.discount.DiscountException;
import com.minimall.domain.order.discount.MemberDiscountPolicy;
import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Arrays;
import java.util.Optional;

@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OrderAmount {

    private Integer originalAmount;        // 할인 적용 전 금액
    private Integer discountAmount;
    private Integer finalAmount;        // 할인 적용 후 금액
    private boolean isDiscounted = false;

    //== 생성자 ==//
    public OrderAmount(int originalAmount) {
        this.originalAmount = originalAmount;
        discountAmount = 0;
        finalAmount = originalAmount;
    }


    //== 비즈니스 로직 ==//
    public void applyDiscount(Member member, MemberDiscountPolicy... policies) {
        checkAlreadyDiscounted();
        applyMemberDiscount(member, policies);
        finalizeDiscount();
    }

    private void applyMemberDiscount(Member member, MemberDiscountPolicy[] policies) {
        discountAmount = Arrays.stream(policies)
                .mapToInt(p -> p.discount(member, originalAmount))
                .sum();
    }

    private void finalizeDiscount() {
        finalAmount = originalAmount - discountAmount;
        discountAmountValidate();
        isDiscounted = true;
    }


    //== 검증 로직 ==//
    private void discountAmountValidate() {
        if (finalAmount < 0) {
            throw DiscountException.discountAmountGreaterThanOrderAmount(originalAmount, discountAmount);
        }
    }

    private void checkAlreadyDiscounted() {
        if (isDiscounted) {
            throw DiscountException.alreadyDiscounted();
        }
    }
}