package com.minimall.domain.order.sub.delivery;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DeliveryRepository extends JpaRepository<Delivery, Long> {

    Optional<Delivery> findByTrackingNo(String trackingNo);

    List<Delivery> findByDeliveryStatus(DeliveryStatus status);
}
