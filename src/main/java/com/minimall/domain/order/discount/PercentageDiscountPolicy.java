package com.minimall.domain.order.discount;

import com.minimall.domain.member.Member;

public class PercentageDiscountPolicy implements MemberDiscountPolicy{

    @Override
    public int discount(Member member, int originalAmount) {
        return switch (member.getGrade()) {
            case BRONZE -> 0;
            case SILVER -> originalAmount * 5 / 100;
            case GOLD -> originalAmount * 10 / 100;
            case VIP -> originalAmount * 20 / 100;
            default -> throw new IllegalArgumentException("Unknown Grade: " + member.getGrade());
        };
    }
}
