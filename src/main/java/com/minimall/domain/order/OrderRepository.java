package com.minimall.domain.order;

import com.minimall.domain.member.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {

    List<Order> findByMember(Member member);

    List<Order> findByMemberAndOrderStatus(Member member, OrderStatus orderStatus);
}
