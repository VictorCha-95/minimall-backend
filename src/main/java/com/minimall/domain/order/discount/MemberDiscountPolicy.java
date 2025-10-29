package com.minimall.domain.order.discount;

import com.minimall.domain.member.Member;

public interface MemberDiscountPolicy {

    int discount(Member member, int originalAmount);
}
