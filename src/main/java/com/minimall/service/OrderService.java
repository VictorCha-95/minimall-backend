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

    //== 비즈니스 로직 ==//
    public OrderCreateResponseDto createOrder(OrderCreateRequestDto request) {

        Member member = getMember(request);

        List<OrderItem> orderItems = request.items().stream()
                .map(this::toOrderItem)
                .toList();

        Order order = Order.createOrder(member, orderItems.toArray(OrderItem[]::new));
        orderRepository.save(order);

        return orderMapper.toCreateResponse(order);
    }


    //== 헬퍼 메서드 ==//
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
