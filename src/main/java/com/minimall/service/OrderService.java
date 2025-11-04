package com.minimall.service;

import com.minimall.domain.member.Member;
import com.minimall.domain.member.MemberRepository;
import com.minimall.domain.order.Order;
import com.minimall.domain.order.OrderItem;
import com.minimall.domain.order.OrderRepository;
import com.minimall.domain.order.dto.OrderMapper;
import com.minimall.domain.order.dto.request.OrderCreateRequestDto;
import com.minimall.domain.order.dto.request.OrderItemCreateDto;
import com.minimall.domain.product.Product;
import com.minimall.domain.product.ProductRepository;
import com.minimall.service.exception.MemberNotFoundException;
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

    public Long createOrder(OrderCreateRequestDto request) {

        // 1. Member 조회
        Member member = getMember(request);

        // 2. OrderItem 생성
        List<OrderItem> orderItems = request.items().stream()
                .map(this::toOrderItem)
                .toList();

        // 3. Order 생성
        Order order = Order.createOrder(member, orderItems.toArray(OrderItem[]::new));

        // 4. 저장
        orderRepository.save(order);

        return order.getId();
    }

    private Member getMember(OrderCreateRequestDto request) {
        return memberRepository.findById(request.memberId())
                .orElseThrow(() -> new MemberNotFoundException("id", request.memberId()));
    }

    private OrderItem toOrderItem(OrderItemCreateDto item) {
        Product product = productRepository.findById(item.productId())
                .orElseThrow(() -> new ProductNotFoundException("id", item.productId()));

        return OrderItem.createOrderItem(product, item.quantity());
    }

}
