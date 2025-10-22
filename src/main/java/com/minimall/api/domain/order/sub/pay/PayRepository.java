package com.minimall.api.domain.order.sub.pay;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PayRepository extends JpaRepository<Pay, Long> {

    List<Pay> findByPayStatus(PayStatus status);

    //TODO Querydsl을 이용하여 PaidAt 날짜 기준으로 구현
}
