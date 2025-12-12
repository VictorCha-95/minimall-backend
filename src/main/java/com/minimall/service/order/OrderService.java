package com.minimall.service.order;

import com.minimall.api.order.pay.dto.PayApiMapper;
import com.minimall.domain.embeddable.Address;
import com.minimall.domain.embeddable.InvalidAddressException;
import com.minimall.domain.exception.Guards;
import com.minimall.domain.member.Member;
import com.minimall.domain.member.MemberRepository;
import com.minimall.domain.order.*;
import com.minimall.domain.order.delivery.DeliveryException;
import com.minimall.domain.order.delivery.DeliveryStatus;
import com.minimall.service.order.dto.DeliveryServiceMapper;
import com.minimall.service.order.dto.*;
import com.minimall.domain.product.Product;
import com.minimall.domain.product.ProductRepository;
import com.minimall.service.exception.MemberNotFoundException;
import com.minimall.service.exception.OrderNotFoundException;
import com.minimall.service.exception.ProductNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
@Transactional
public class OrderService {

    private final OrderRepository orderRepository;
    private final MemberRepository memberRepository;
    private final ProductRepository productRepository;
    private final OrderServiceMapper orderServiceMapper;
    private final PayApiMapper payApiMapper;
    private final DeliveryServiceMapper deliveryServiceMapper;
    
    //== 주문 생성 ==//
    public Order createOrder(OrderCreateCommand command) {

        Member member = findMember(command.memberId());

        Order order = Order.createOrder(
                member,
                command.items().stream()
                        .map(this::toOrderItem)
                        .toArray(OrderItem[]::new));

        orderRepository.save(order);

        return order;
    }

    private OrderItem toOrderItem(OrderItemCreateCommand command) {
        Long productId = command.productId();
        Product product = findProductById(productId);
        return OrderItem.createOrderItem(product, command.quantity());
    }

    //== 주문 조회 ==//
    @Transactional(readOnly = true)
    public OrderDetailResult getOrderDetail(Long id) {
        Order order = findOrderById(id);
        return orderServiceMapper.toDetailResult(order);
    }

    @Transactional(readOnly = true)
    public List<OrderSummaryResult> getOrderSummaries(Long memberId) {
        Member member = findMember(memberId);
        List<Order> orders = orderRepository.findByMember(member);
        return orderServiceMapper.toSummaryResultList(orders);
    }

    //== 결제 ==//
    public Pay processPayment(Long id, PayCommand command) {
        Order order = findOrderById(id);
        Pay pay = order.processPayment(payApiMapper.toEntity(command));
        return pay;
    }

    /**
     * @param shipAddr
     * 배송 주소 null -> 회원 주소로 배송(회원 주소도 없으면 예외 발생)
     */
    //== 배송 ==//
    public DeliverySummaryResult prepareDelivery(Long id, Address shipAddr) {
        Order order = findOrderById(id);

        Address resolvedAddr = resolveAddr(shipAddr, order);
        order.prepareDelivery(resolvedAddr);

        return deliveryServiceMapper.toDeliverySummary(order.getDelivery());
    }

    private static Address resolveAddr(Address shipAddr, Order order) {
        return Objects.requireNonNullElseGet(
                shipAddr,
                () -> getMemberAddr(order)
        );
    }

    private static Address getMemberAddr(Order order) {
        return Optional.of(order.getMember())
                .map(Member::getAddr)
                .orElseThrow(InvalidAddressException::required);
    }

    public void startDelivery(Long id, String trackingNo,LocalDateTime shippedAt) {
        Order order = findOrderById(id);
        order.startDelivery(trackingNo, shippedAt);
    }

    public void completeDelivery(Long id, LocalDateTime arrivedAt) {
        Order order = findOrderById(id);
        Delivery delivery = order.getDelivery();
        Guards.requireNotNull(delivery, DeliveryException::isNull);

        if (delivery.getDeliveryStatus() == DeliveryStatus.COMPLETED) {
            return;
        }

        order.completeDelivery(arrivedAt);
    }

    //== 헬퍼 메서드 ==//
    private Order findOrderById(Long orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException("id", orderId));
    }

    private Product findProductById(Long productId) {
        return productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException("id", productId));
    }

    private Member findMember(Long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new MemberNotFoundException("id", memberId));
    }

}
