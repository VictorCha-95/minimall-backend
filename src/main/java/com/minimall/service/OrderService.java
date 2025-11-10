package com.minimall.service;

import com.minimall.domain.member.Member;
import com.minimall.domain.member.MemberRepository;
import com.minimall.domain.order.Order;
import com.minimall.domain.order.OrderItem;
import com.minimall.domain.order.OrderRepository;
import com.minimall.domain.order.dto.OrderMapper;
import com.minimall.domain.order.dto.request.OrderCreateRequestDto;
import com.minimall.domain.order.dto.request.OrderItemCreateDto;
import com.minimall.domain.order.dto.response.OrderCreateResponseDto;
import com.minimall.domain.order.dto.response.OrderDetailResponseDto;
import com.minimall.domain.order.dto.response.OrderSummaryResponseDto;
import com.minimall.domain.product.Product;
import com.minimall.domain.product.ProductRepository;
import com.minimall.service.exception.MemberNotFoundException;
import com.minimall.service.exception.OrderNotFoundException;
import com.minimall.service.exception.ProductNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class OrderService {

    private final OrderRepository orderRepository;
    private final MemberRepository memberRepository;
    private final ProductRepository productRepository;
    private final OrderMapper orderMapper;
    
    //== 주문 생성 ==//
    public OrderCreateResponseDto createOrder(OrderCreateRequestDto request) {

        Member member = findMember(request);

        List<OrderItem> orderItems = createOrderItems(request);

        Order order = Order.createOrder(member, orderItems.toArray(OrderItem[]::new));
        orderRepository.save(order);

        return orderMapper.toCreateResponse(order);
    }

    //== 주문 조회 ==//
    public OrderDetailResponseDto getOrderDetail(Long id) {
        Order order = findOrder(id);
        return orderMapper.toOrderDetailResponse(order);
    }

    public List<OrderSummaryResponseDto> getOrderSummaries(Long memberId) {
        Member member = findMember(memberId);
        List<Order> orders = orderRepository.findByMember(member);
        return orderMapper.toOrderSummaryResponse(orders);
    }

    //== 헬퍼 메서드 ==//
    private Order findOrder(Long orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException("id", orderId));
    }

    private Member findMember(OrderCreateRequestDto request) {
        return memberRepository.findById(request.memberId())
                .orElseThrow(() -> new MemberNotFoundException("id", request.memberId()));
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
