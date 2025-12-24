package com.minimall.domain.order.discount;

import com.minimall.domain.member.Member;

public class FixedDiscountPolicy implements MemberDiscountPolicy{

    @Override
    public int discount(Member member, int originalAmount) {
        return switch (member.getCustomerGrade()) {
            case BRONZE -> 0;
            case SILVER -> 1000;
            case GOLD -> 2000;
            case VIP -> 5000;
            default -> throw new IllegalArgumentException("Unknown Grade: " + member.getCustomerGrade());
        };
    }
}
