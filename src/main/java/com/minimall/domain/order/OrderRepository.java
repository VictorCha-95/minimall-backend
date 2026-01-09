package com.minimall.domain.order;

import com.minimall.domain.member.Member;
import com.minimall.service.order.dto.result.OrderSummaryProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {

    List<Order> findByMember(Member member);

    @Query("select distinct o from Order o left join fetch o.orderItems where o.member.id = :memberId")
    List<Order> findByMemberIdWithItems(@Param("memberId") Long memberId);

    @Query("""
            select o.id as id,
                   o.orderedAt as orderedAt,
                   o.orderStatus as orderStatus,
                   count(oi.id) as itemCount,
                   o.orderAmount.finalAmount as finalAmount
            from Order o
            left join o.orderItems oi
            where o.member.id = :memberId
            group by o.id, o.orderedAt, o.orderStatus, o.orderAmount.finalAmount
            """)
    List<OrderSummaryProjection> findOrderSummariesByMemberId(@Param("memberId") Long memberId);

    List<Order> findByOrderStatus(OrderStatus orderStatus);

    List<Order> findByMemberAndOrderStatus(Member member, OrderStatus orderStatus);
}
