package com.minimall.api.domain.order.sub.delivery;

import com.minimall.api.domain.embeddable.Address;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DeliveryRepository extends JpaRepository<Delivery, Long> {

    Optional<Delivery> findByTrackingNo(String trackingNo);

    List<Delivery> findByDeliveryStatus(DeliveryStatus status);
}
