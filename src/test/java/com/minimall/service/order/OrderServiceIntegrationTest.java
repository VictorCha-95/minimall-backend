package com.minimall.service.order;

import com.minimall.domain.embeddable.Address;
import com.minimall.domain.member.Member;
import com.minimall.domain.member.MemberRepository;
import com.minimall.domain.order.Order;
import com.minimall.domain.order.OrderRepository;
import com.minimall.domain.order.OrderStatus;
import com.minimall.domain.order.dto.request.OrderCreateRequestDto;
import com.minimall.domain.order.dto.request.OrderItemCreateDto;
import com.minimall.domain.order.dto.response.OrderCreateResponseDto;
import com.minimall.domain.product.Product;
import com.minimall.domain.product.ProductRepository;
import com.minimall.service.OrderService;
import com.minimall.service.exception.MemberNotFoundException;
import com.minimall.service.exception.ProductNotFoundException;
import jakarta.persistence.EntityManager;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.assertj.core.api.SoftAssertions.*;

@SpringBootTest
@ActiveProfiles("integration-test")
@Testcontainers
@Transactional
public class OrderServiceIntegrationTest {

    @ServiceConnection
    static final MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.0")
            .withReuse(true);

    @Autowired
    OrderService orderService;

    @Autowired
    OrderRepository orderRepository;

    @Autowired
    MemberRepository memberRepository;

    @Autowired
    ProductRepository productRepository;

    @Autowired
    EntityManager em;

    @Autowired
    TransactionTemplate tx;

    private Member member;
    private Product book;
    private Product keyboard;

    static final int ORDER_QUANTITY = 10;
    static final long NOT_EXISTS_ID = 999_999_999_999_999L;

    @BeforeEach
    void setUp() {

        //== Member Entity ==//
        member = Member.builder()
                .loginId("user1")
                .password("abc12345")
                .name("차태승")
                .email("cts9458@naver.com")
                .addr(new Address("12345", "광주광역시", "광산구", "수등로76번길 40", "123동 1501호"))
                .build();

        //== Product Entity ==//
        keyboard = new Product("키보드", 100000, 20);
        book = new Product("도서", 20000, 50);
    }

    void flushClear() {
        em.flush();
        em.clear();
    }

    @Nested
    @DisplayName("createOrder(createRequestDto)")
    class CreateOrder {
        @Test
        @DisplayName("주문 생성 - DB 반영 및 기타 검증")
        void success() {
            //given
            Member savedMember = memberRepository.save(member);
            Product savedKeyboard = productRepository.save(keyboard);
            Product savedBook = productRepository.save(book);

            OrderCreateRequestDto request = new OrderCreateRequestDto(
                    savedMember.getId(),
                    List.of(new OrderItemCreateDto(savedKeyboard.getId(), ORDER_QUANTITY),
                            new OrderItemCreateDto(savedBook.getId(), ORDER_QUANTITY)));

            //when
            OrderCreateResponseDto order = orderService.createOrder(request);
            flushClear();

            //then: DB 조회해서 검증
            Order found = orderRepository.findById(order.id())
                    .orElseThrow(() -> new AssertionError("주문이 저장되지 않음"));

            assertSoftly(softly -> {
                softly.assertThat(found.getId()).isEqualTo(order.id());
                softly.assertThat(found.getOrderStatus()).isEqualTo(OrderStatus.ORDERED);
                softly.assertThat(found.getOrderedAt()).isNotNull();
                softly.assertThat(found.getMember().getId()).isEqualTo(savedMember.getId());
                softly.assertThat(found.getOrderItems()).hasSize(2);
                softly.assertThat(found.getPay()).isNull();
                softly.assertThat(found.getDelivery()).isNull();
            });

            //then: 주문 금액 검증
            int expectedAmount = (keyboard.getPrice() * ORDER_QUANTITY) + (book.getPrice() * ORDER_QUANTITY);

            assertSoftly(softly -> {
                softly.assertThat(found.getOrderAmount().getOriginalAmount()).isEqualTo(expectedAmount);
                softly.assertThat(found.getOrderAmount().getFinalAmount()).isEqualTo(expectedAmount);
                softly.assertThat(found.getOrderAmount().getDiscountAmount()).isZero();
            });

            //then: 상품 재고 차감 검증
            Product bookAfter = productRepository.findById(book.getId()).orElseThrow();
            Product keyboardAfter = productRepository.findById(keyboard.getId()).orElseThrow();

            assertSoftly(softly -> {
                softly.assertThat(keyboardAfter.getStockQuantity()).isEqualTo(20 - ORDER_QUANTITY);
                softly.assertThat(bookAfter.getStockQuantity()).isEqualTo(50 - ORDER_QUANTITY);
            });
        }

        @Test
        @DisplayName("회원 미존재 -> 예외 + 롤백(주문 미생성)")
        void shouldRollBack_whenMemberNotFound() {
            //given
            Product product = productRepository.save(new Product("p", 50_000, 50));
            long beforeOrderCount = orderRepository.count();
            long beforeStock = productRepository.findById(product.getId()).orElseThrow().getStockQuantity();

            OrderCreateRequestDto request = new OrderCreateRequestDto(
                    NOT_EXISTS_ID,
                    List.of(new OrderItemCreateDto(product.getId(), ORDER_QUANTITY)));



            //when
            assertThatThrownBy(() -> orderService.createOrder(request))
                    .isInstanceOf(MemberNotFoundException.class);

            flushClear();

            //then: 롤백: 주문 미생성, 주문 상품 재고 변화 없음
            assertSoftly(softly -> {
                softly.assertThat(orderRepository.count()).isEqualTo(beforeOrderCount);
                softly.assertThat(productRepository.findById(product.getId()).orElseThrow().getStockQuantity())
                        .isEqualTo(beforeStock);
            });
        }

        @Test
        @DisplayName("상품 미존재 -> 예외 + 롤백(주문 미생성)")
        @Transactional(propagation = Propagation.NOT_SUPPORTED)
        void shouldRollBack_whenProductNotFound() {
            //given
            Member m = memberRepository.save(member);
            Product exists = productRepository.save(new Product("p", 10_000, 50));
            long beforeOrderCount = orderRepository.count();
            Integer beforeStock = exists.getStockQuantity();

            OrderCreateRequestDto request = new OrderCreateRequestDto(
                    m.getId(),
                    List.of(new OrderItemCreateDto(exists.getId(), ORDER_QUANTITY),
                            new OrderItemCreateDto(NOT_EXISTS_ID, ORDER_QUANTITY)));

            //when
            assertThatThrownBy(() -> orderService.createOrder(request))
                    .isInstanceOf(ProductNotFoundException.class);

            //then: 롤백: 주문 미생성, 실제 존재하는 주문 상품 재고 변화 없음
            assertSoftly(softly -> {
                softly.assertThat(orderRepository.count()).isEqualTo(beforeOrderCount);
                softly.assertThat(productRepository.findById(exists.getId()).orElseThrow().getStockQuantity())
                        .isEqualTo(beforeStock);
            });
        }


    }

}
