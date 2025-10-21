package com.minimall.api.domain.order.sub.discount;

import com.minimall.api.domain.member.Member;

public interface MemberDiscountPolicy {

    int discount(Member member, int originalAmount);
}
