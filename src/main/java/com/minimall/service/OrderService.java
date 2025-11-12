package com.minimall.service;

import com.minimall.domain.embeddable.Address;
import com.minimall.domain.embeddable.InvalidAddressException;
import com.minimall.domain.member.Member;
import com.minimall.domain.member.MemberRepository;
import com.minimall.domain.order.Delivery;
import com.minimall.domain.order.Order;
import com.minimall.domain.order.OrderItem;
import com.minimall.domain.order.OrderRepository;
import com.minimall.domain.order.delivery.DeliveryStatus;
import com.minimall.domain.order.delivery.dto.DeliveryMapper;
import com.minimall.domain.order.delivery.dto.DeliverySummaryDto;
import com.minimall.domain.order.dto.OrderMapper;
import com.minimall.domain.order.dto.request.OrderCreateRequestDto;
import com.minimall.domain.order.dto.request.OrderItemCreateDto;
import com.minimall.domain.order.dto.response.OrderCreateResponseDto;
import com.minimall.domain.order.dto.response.OrderDetailResponseDto;
import com.minimall.domain.order.dto.response.OrderSummaryResponseDto;
import com.minimall.domain.order.pay.dto.PayMapper;
import com.minimall.domain.order.pay.dto.PayRequestDto;
import com.minimall.domain.order.pay.dto.PaySummaryDto;
import com.minimall.domain.product.Product;
import com.minimall.domain.product.ProductRepository;
import com.minimall.service.exception.MemberNotFoundException;
import com.minimall.service.exception.OrderNotFoundException;
import com.minimall.service.exception.ProductNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class OrderService {

    private final OrderRepository orderRepository;
    private final MemberRepository memberRepository;
    private final ProductRepository productRepository;
    private final OrderMapper orderMapper;
    private final PayMapper payMapper;
    private final DeliveryMapper deliveryMapper;
    
    //== 주문 생성 ==//
    public OrderCreateResponseDto createOrder(OrderCreateRequestDto request) {

        Member member = findMember(request.memberId());

        List<OrderItem> orderItems = createOrderItems(request);

        Order order = Order.createOrder(member, orderItems.toArray(OrderItem[]::new));
        orderRepository.save(order);

        return orderMapper.toCreateResponse(order);
    }

    //== 주문 조회 ==//
    @Transactional(readOnly = true)
    public OrderDetailResponseDto getOrderDetail(Long id) {
        Order order = findOrderById(id);
        return orderMapper.toOrderDetailResponse(order);
    }

    @Transactional(readOnly = true)
    public List<OrderSummaryResponseDto> getOrderSummaries(Long memberId) {
        Member member = findMember(memberId);
        List<Order> orders = orderRepository.findByMember(member);
        return orderMapper.toOrderSummaryResponse(orders);
    }

    //== 결제 ==//
    public PaySummaryDto processPayment(Long id, PayRequestDto request) {
        Order order = findOrderById(id);
        order.processPayment(payMapper.toEntity(request));
        return payMapper.toPaySummary(order.getPay());
    }

    /**
     * @param shipAddr
     * 배송 주소 null -> 회원 주소로 배송(회원 주소도 없으면 예외 발생)
     */
    //== 배송 ==//
    public DeliverySummaryDto prepareDelivery(Long id, @Nullable Address shipAddr) {
        Order order = findOrderById(id);

        Address resolvedAddr = resolveAddr(shipAddr, order);
        order.prepareDelivery(resolvedAddr);

        return deliveryMapper.toDeliverySummary(order.getDelivery());
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

    public DeliverySummaryDto startDelivery(Long id, String trackingNo, @Nullable LocalDateTime shippedAt) {
        Order order = findOrderById(id);
        order.startDelivery(trackingNo, (shippedAt != null ? shippedAt : LocalDateTime.now()));
        return deliveryMapper.toDeliverySummary(order.getDelivery());
    }

    public DeliverySummaryDto completeDelivery(String trackingNo, @Nullable LocalDateTime arrivedAt) {
        Order order = findOrderByTrackingNo(trackingNo);
        Delivery delivery = order.getDelivery();

        if (delivery.getDeliveryStatus() == DeliveryStatus.COMPLETED) {
            return deliveryMapper.toDeliverySummary(delivery);
        }

        order.completeDelivery(arrivedAt);
        return deliveryMapper.toDeliverySummary(delivery);
    }




    //== 헬퍼 메서드 ==//
    private Order findOrderById(Long orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException("id", orderId));
    }

    private Order findOrderByTrackingNo(String trackingNo) {
        return orderRepository.findByTrackingNo(trackingNo)
                .orElseThrow(() -> new OrderNotFoundException("trackingNo", trackingNo));
    }

    private Member findMember(Long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new MemberNotFoundException("id", memberId));
    }

    private List<OrderItem> createOrderItems(OrderCreateRequestDto request) {
        return request.items().stream()
                .map(this::toOrderItem)
                .toList();
    }

    private OrderItem toOrderItem(OrderItemCreateDto item) {
        Product product = productRepository.findById(item.productId())
                .orElseThrow(() -> new ProductNotFoundException("id", item.productId()));

        return OrderItem.createOrderItem(product, item.quantity());
    }

}
