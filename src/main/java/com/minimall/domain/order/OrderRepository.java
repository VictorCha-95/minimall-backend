package com.minimall.domain.order;

import com.minimall.domain.member.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {

    List<Order> findByMember(Member member);

    List<Order> findByOrderStatus(OrderStatus orderStatus);

    List<Order> findByMemberAndOrderStatus(Member member, OrderStatus orderStatus);

    @Query("select o from Order o join fetch o.delivery d where d.trackingNo = :trackingNo")
    Optional<Order> findByTrackingNo(@Param("trackingNo") String trackingNo);
}
