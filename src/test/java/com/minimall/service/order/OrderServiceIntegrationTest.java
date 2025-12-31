package com.minimall.service.order;

import com.minimall.AbstractIntegrationTest;
import com.minimall.domain.common.DomainType;
import com.minimall.domain.embeddable.Address;
import com.minimall.domain.embeddable.InvalidAddressException;
import com.minimall.domain.member.Member;
import com.minimall.domain.member.MemberRepository;
import com.minimall.domain.order.*;
import com.minimall.domain.order.delivery.DeliveryException;
import com.minimall.domain.order.delivery.DeliveryStatus;
import com.minimall.domain.order.delivery.DeliveryStatusException;
import com.minimall.domain.order.exception.OrderStatusException;
import com.minimall.domain.order.pay.PayAmountMismatchException;
import com.minimall.domain.order.pay.PayMethod;
import com.minimall.domain.order.pay.PayStatus;
import com.minimall.domain.product.Product;
import com.minimall.domain.product.ProductRepository;
import com.minimall.service.exception.MemberNotFoundException;
import com.minimall.service.exception.OrderNotFoundException;
import com.minimall.service.exception.ProductNotFoundException;
import com.minimall.service.order.dto.command.OrderCreateCommand;
import com.minimall.service.order.dto.command.OrderItemCreateCommand;
import com.minimall.service.order.dto.command.PayCommand;
import com.minimall.service.order.dto.result.DeliverySummaryResult;
import com.minimall.service.order.dto.result.OrderDetailResult;
import com.minimall.service.order.dto.result.OrderSummaryResult;
import jakarta.persistence.EntityManager;
import org.hibernate.Transaction;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.test.context.transaction.TestTransaction;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.assertj.core.api.SoftAssertions.*;

@SpringBootTest
@Transactional
public class OrderServiceIntegrationTest extends AbstractIntegrationTest {

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

    private Member member;
    private Member savedMember;
    private Member memberNullAddr;

    private Product book;
    private Product keyboard;

    private OrderCreateCommand createCommand1;
    private OrderCreateCommand createCommand2;
    private OrderCreateCommand requestAddrNull;

    static final int ORDER_QUANTITY = 10;
    static final long NOT_EXISTS_ID = 999_999_999_999_999L;

    private static final String DEFAULT_PASSWORD_HASH = "12345678";
    private static final String DEFAULT_NAME = "차태승";
    private static final Address DEFAULT_ADDRESS =
            Address.createAddress("62550", "광주광역시", "광산구", "수등로76번길 40", "123동 456호");

    @BeforeEach
    void setUp() {
        //== Member Entity ==//
        String loginId1 = UUID.randomUUID().toString();
        member = Member.registerCustomer(loginId1, DEFAULT_PASSWORD_HASH, DEFAULT_NAME, loginId1 + "naver.com", DEFAULT_ADDRESS);

        String loginId2 = UUID.randomUUID().toString();
        memberNullAddr = Member.registerCustomer(loginId2, DEFAULT_PASSWORD_HASH, DEFAULT_NAME, loginId2 + "naver.com", null);

        //== Product Entity ==//
        keyboard = new Product("키보드", 100000, 20);
        book = new Product("도서", 20000, 50);

        //== OrderCreateRequest ==//
        savedMember = memberRepository.save(member);
        Member savedMemberAddrNull = memberRepository.save(memberNullAddr);
        Product savedKeyboard = productRepository.save(keyboard);
        Product savedBook = productRepository.save(book);
        createCommand1 = new OrderCreateCommand(
                savedMember.getId(),
                List.of(new OrderItemCreateCommand(savedKeyboard.getId(), ORDER_QUANTITY),
                        new OrderItemCreateCommand(savedBook.getId(), ORDER_QUANTITY)));

        createCommand2 = new OrderCreateCommand(
                savedMember.getId(),
                List.of(new OrderItemCreateCommand(savedKeyboard.getId(), 5),
                        new OrderItemCreateCommand(savedBook.getId(), 5)));

        requestAddrNull = new OrderCreateCommand(
                savedMemberAddrNull.getId(),
                List.of(new OrderItemCreateCommand(savedKeyboard.getId(), 5),
                        new OrderItemCreateCommand(savedBook.getId(), 5)));

    }

    void flushClear() {
        em.flush();
        em.clear();
    }

    @Nested
    @DisplayName("createOrder(OrderCreateCommand)")
    class CreateOrder {
        @Test
        @DisplayName("주문 생성 - DB 반영 및 기타 검증")
        void success() {
            //when
            Order order = orderService.createOrder(createCommand1);
            flushClear();

            //then: DB 조회해서 실제 생성된 주문 검증
            Order found = orderRepository.findById(order.getId())
                    .orElseThrow(() -> new AssertionError("주문이 저장되지 않음"));

            assertSoftly(softly -> {
                softly.assertThat(found.getId()).isEqualTo(order.getId());
                softly.assertThat(found.getOrderStatus()).isEqualTo(OrderStatus.ORDERED);
                softly.assertThat(found.getOrderedAt()).isNotNull();
                softly.assertThat(found.getMember().getId()).isEqualTo(member.getId());
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

            OrderCreateCommand command = new OrderCreateCommand(
                    NOT_EXISTS_ID,
                    List.of(new OrderItemCreateCommand(product.getId(), ORDER_QUANTITY)));

            //when
            assertThatThrownBy(() -> orderService.createOrder(command))
                    .isInstanceOf(MemberNotFoundException.class);

            flushClear();

            //then: 롤백 -> 주문 미생성, 주문 상품 재고 변화 없음
            assertSoftly(softly -> {
                softly.assertThat(orderRepository.count()).isEqualTo(beforeOrderCount);
                softly.assertThat(productRepository.findById(product.getId()).orElseThrow().getStockQuantity())
                        .isEqualTo(beforeStock);
            });
        }

        @Test
        @DisplayName("상품 미존재 -> 예외 + 롤백(주문 미생성)")
        void shouldRollBack_whenProductNotFound() {
            //given
            //=====================
            //(Tx1: Setup -> Commit)
            //=====================
            Product exists = productRepository.save(new Product("p", 10_000, 50));

            Long existsId = exists.getId();
            Long memberId = member.getId();

            TestTransaction.flagForCommit();
            TestTransaction.end();

            //=====================
            //(Tx2: Action -> Rollback)
            //=====================
            TestTransaction.start();

            long beforeOrderCount = orderRepository.count();

            Integer beforeStock = productRepository.findById(existsId)
                    .orElseThrow()
                    .getStockQuantity(); // 50

            OrderCreateCommand command = new OrderCreateCommand(
                    memberId,
                    List.of(new OrderItemCreateCommand(existsId, ORDER_QUANTITY), // 존재 ID로 주문 생성
                            new OrderItemCreateCommand(NOT_EXISTS_ID, ORDER_QUANTITY))); // 미존재 ID로 주문 생성


            //when
            assertThatThrownBy(() -> orderService.createOrder(command))
                    .isInstanceOf(ProductNotFoundException.class);

            //예외 발생했으니 롤백으로 종료
            TestTransaction.flagForRollback();
            TestTransaction.end();

            //=====================
            //(Tx3: Verify)
            //=====================
            TestTransaction.start();

            //then: 롤백: 주문 미생성, 실제 존재하는 주문 상품 재고 변화 없음
            assertSoftly(softly -> {
                softly.assertThat(orderRepository.count()).isEqualTo(beforeOrderCount);
                softly.assertThat(productRepository.findById(existsId).orElseThrow().getStockQuantity())
                        .isEqualTo(beforeStock);
            });

            // 검증용 트랜잭션도 DB 오염 방지 위해 롤백 종료
            TestTransaction.flagForRollback();
            TestTransaction.end();
        }
    }

    @Nested
    @DisplayName("cancelOrder(Long)")
    class CancelOrder{
        @DisplayName("주문 취소: DB 주문 조회 -> 취소")
        @Test
        void success(){
            //given
            Order order = orderService.createOrder(createCommand1);

            //when
            orderService.cancelOrder(order.getId());

            //then
            assertThat(order.getOrderStatus()).isEqualTo(OrderStatus.CANCELED);
        }

        @DisplayName("주문 미존재 -> Not Found 예외 발생")
        @Test
        void shouldFail_whenOrderNotFound() {
            //given
            Long wrongId = 9999999L;

            //when - then
            assertThatThrownBy(() -> orderService.cancelOrder(wrongId))
                    .isInstanceOfSatisfying(OrderNotFoundException.class, e -> {
                        assertThat(e.getMessage()).contains("id");
                    });
        }
    }

    @Nested
    @DisplayName("getOrderDetail(Long)")
    class GetOrderDetail{
        @Test
        @DisplayName("주문 상세 단건 조회: 식별자, 일시, 상태, 총금액, 주문 항목, 결제, 배송 응답")
        void success() {
            //given
            Order order = orderService.createOrder(createCommand1);

            //when
            OrderDetailResult result = orderService.getOrderDetail(order.getId());

            //then
            assertSoftly(softly -> {
                softly.assertThat(result.id()).isNotNull();
                softly.assertThat(result.orderedAt()).isNotNull();
                softly.assertThat(result.orderStatus()).isEqualTo(OrderStatus.ORDERED);
                softly.assertThat(result.orderItems()).hasSize(2);
            });
        }

        @Test
        @DisplayName("주문 없음: OrderNotFoundException")
        void shouldFail_whenOrderNotFound() {
            //given
            Long invalidId = 999_999_999L;

            //then
            assertThatThrownBy(() -> orderService.getOrderDetail(invalidId))
                    .isInstanceOfSatisfying(OrderNotFoundException.class, e ->
                            assertThat(e.getMessage()).contains("id", String.valueOf(invalidId), DomainType.ORDER.getDisPlayName())
                    );
        }
    }

    @Nested
    @DisplayName("getOrderSummaries(Long)")
    class GetOrderSummaries {
        @Test
        @DisplayName("주문 목록 요약 조회: ")
        void success() {
            //given
            orderService.createOrder(createCommand1);
            orderService.createOrder(createCommand2);

            //when
            List<OrderSummaryResult> result = orderService.getOrderSummaries(member.getId());

            //then
            assertSoftly(softly -> {
                softly.assertThat(result).hasSize(2);
                softly.assertThat(result.getFirst().id()).isNotNull();
                softly.assertThat(result.getFirst().orderedAt()).isNotNull();
                softly.assertThat(result.getFirst().orderStatus()).isEqualTo(OrderStatus.ORDERED);
                softly.assertThat(result.getFirst().itemCount()).isEqualTo(2);
                softly.assertThat(result.getFirst().finalAmount()).isNotNull();
            });
        }

        @Test
        @DisplayName("회원 주문 없음: 빈 리스트 반환")
        void returnEmpty_whenOrderIsEmpty() {
            //given
            Member member = Member.registerCustomer("user12345", "12345", "박지성", "ex@ex.com", null);
            Member savedMember = memberRepository.save(member);

            //when
            List<OrderSummaryResult> result = orderService.getOrderSummaries(savedMember.getId());

            //then
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("processPayment(Long, PayCommand)")
    class ProcessPayment {
        @Test
        @DisplayName("결제: 주문 조회 -> 결제 -> 매퍼 dto 변환")
        void success() {
            //given
            Order order = orderService.createOrder(createCommand1);
            PayCommand command = new PayCommand(PayMethod.CARD, order.getOrderAmount().getFinalAmount());

            //when
            Pay result = orderService.processPayment(order.getId(), command);

            //then
            assertSoftly(softly -> {
                softly.assertThat(result.getPayAmount()).isEqualTo(order.getOrderAmount().getFinalAmount());
            });
        }

        @Test
        @DisplayName("중복 결제 -> 예외 발생 + 기존 결제/주문 상태 유지")
        void shouldFail_whenDuplicatedPay() {
            //given
            Order order = orderService.createOrder(createCommand1);
            PayCommand command = new PayCommand(PayMethod.CARD, order.getOrderAmount().getFinalAmount());
            orderService.processPayment(order.getId(), command);

            //then
            assertThatThrownBy(() -> orderService.processPayment(order.getId(), command))
                    .isInstanceOf(OrderStatusException.class);

            Order foundOrder = orderRepository.findById(order.getId()).get();
            assertThat(foundOrder.getOrderStatus()).isEqualTo(OrderStatus.CONFIRMED);
            assertThat(foundOrder.getPay().getPayStatus()).isEqualTo(PayStatus.PAID);
        }

        @Test
        @DisplayName("주문 금액, 결제 금액 불일치 -> 예외 발생 + 주문 상태는 ORDERED 유지")
        void shouldFail_whenMismatchAmount() {
            //given
            Order order = orderService.createOrder(createCommand1);

            int invalidAmount = 999_999;
            PayCommand command = new PayCommand(PayMethod.CARD, invalidAmount);

            //then
            assertThatThrownBy(() -> orderService.processPayment(order.getId(), command))
                    .isInstanceOf(PayAmountMismatchException.class);

            Order foundOrder = orderRepository.findById(order.getId()).get();
            assertThat(foundOrder.getPay().getPayStatus()).isEqualTo(PayStatus.FAILED);
            assertThat(foundOrder.getOrderStatus()).isEqualTo(OrderStatus.ORDERED);
        }
    }

    @Nested
    @DisplayName("prepareDelivery(Long, Address)")
    class PrepareDelivery {
        @Test
        @DisplayName("배송 준비: 결제된 주문 조회 -> delivery 생성 및 주소 세팅 -> address dto 변환 -> delivery dto 변환")
        void success() {
            //given
            Long orderId = createOrderAndProcessPayment(createCommand1);
            Address shipAddr = createSampleAddr();

            //when
            DeliverySummaryResult result = orderService.prepareDelivery(orderId, shipAddr);

            //then
            assertSoftly(softly -> {
                softly.assertThat(result.deliveryStatus()).isEqualTo(DeliveryStatus.READY);
                softly.assertThat(result.trackingNo()).isNull();
                softly.assertThat(result.shipAddr().city()).isEqualTo(shipAddr.getCity());
            });
        }

        @Test
        @DisplayName("회원 주소 존재 / 배송 주소 null: 결제된 주문 조회 -> 회원 주소 조회 -> delivery 생성 및 주소 세팅 -> address dto 변환 -> delivery dto 변환")
        void success_whenShipAddrIsNullAndMemberAddrIsNotNull() {
            //given
            Long orderId = createOrderAndProcessPayment(createCommand1);

            //when
            DeliverySummaryResult result = orderService.prepareDelivery(orderId, null);

            //then
            assertSoftly(softly -> {
                softly.assertThat(result.deliveryStatus()).isEqualTo(DeliveryStatus.READY);
                softly.assertThat(result.trackingNo()).isNull();
                softly.assertThat(result.shipAddr().city()).isEqualTo(member.getAddr().getCity());
            });
        }

        @Test
        @DisplayName("회원 주소 null / 배송 주소 null: 예외 InvalidAddressException")
        void shouldFail_whenShipAddrAndMemberAddrIsNull() {
            //given
            Long orderId = createOrderAndProcessPayment(requestAddrNull);
            Member member = memberRepository.findById(requestAddrNull.memberId()).get();
            assertThat(member.getAddr()).isNull();

            //when-then
            assertThatThrownBy(() -> orderService.prepareDelivery(orderId, null))
                    .isInstanceOfSatisfying(InvalidAddressException.class, e -> {
                        assertThat(e.getReason()).isEqualTo(InvalidAddressException.Reason.REQUIRED);
                    });
        }
    }

    @Nested
    @DisplayName("startDelivery(Long, String, LocalDatetime")
    class StartDelivery {

        String trackingNo = "98765";
        LocalDateTime shippedAt = LocalDateTime.of(2025, 11, 12, 8, 30);

        @Test
        @DisplayName("배송 시작: 배송 준비된 주문 조회 -> trackingNo, shippedAt 설정")
        void success() {
            //given
            Long orderId = createOrderAndProcessPayment(createCommand1);
            Address shipAddr = createSampleAddr();
            orderService.prepareDelivery(orderId, shipAddr);

            //when
            orderService.startDelivery(orderId, trackingNo, shippedAt);
            Delivery result = orderRepository.findById(orderId).get().getDelivery();

            //then
            assertSoftly(softly -> {
                softly.assertThat(result.getDeliveryStatus()).isEqualTo(DeliveryStatus.SHIPPING);
                softly.assertThat(result.getTrackingNo()).isEqualTo(trackingNo);
                softly.assertThat(result.getShippedAt()).isEqualTo(shippedAt);
            });
        }

        @Test
        @DisplayName("결제 되지 않은 상태 -> 예외")
        void shouldFail_whenNotPaid() {
            //given
            Order order = orderService.createOrder(createCommand1);

            //when-then
            assertThatThrownBy(() -> orderService.startDelivery(order.getId(), trackingNo, shippedAt))
                    .isInstanceOf(DeliveryException.class);
        }

        @Test
        @DisplayName("배송 준비 되지 않은 상태 -> 예외")
        void shouldFail_whenNotPrepared() {
            //given
            Long orderId = createOrderAndProcessPayment(createCommand1);

            //when-then
            assertThatThrownBy(() -> orderService.startDelivery(orderId, trackingNo, shippedAt))
                    .isInstanceOf(DeliveryException.class);
        }
    }

    @Nested
    @DisplayName("completeDelivery(Long, LocalDatetime)")
    class CompleteDelivery {

        LocalDateTime arrivedAt = LocalDateTime.of(2025, 12, 1, 8, 30);

        @Test
        @DisplayName("배송 완료: 배송 중인 주문 운송장 번호로 조회 -> 도착 시간 설정")
        void success() {
            //given
            Long orderId = createOrderAndProcessPayment(createCommand1);
            Address shipAddr = createSampleAddr();
            orderService.prepareDelivery(orderId, shipAddr);
            startDelivery(orderId);

            //when
            orderService.completeDelivery(orderId, arrivedAt);
            Delivery delivery = orderRepository.findById(orderId).get().getDelivery();

            //then
            assertSoftly(softly -> {
                softly.assertThat(delivery.getDeliveryStatus()).isEqualTo(DeliveryStatus.COMPLETED);
                softly.assertThat(delivery.getArrivedAt()).isEqualTo(arrivedAt);
            });
        }

        @Test
        @DisplayName("도착 시간 null -> 지금 시간으로 설정")
        void success_whenArrivedAtIsNull() {
            //given
            Long orderId = createOrderAndProcessPayment(createCommand1);
            Address shipAddr = createSampleAddr();
            orderService.prepareDelivery(orderId, shipAddr);
            startDelivery(orderId);

            //when
            orderService.completeDelivery(orderId, null);
            Delivery delivery = orderRepository.findById(orderId).get().getDelivery();

            //then
            assertSoftly(softly -> {
                softly.assertThat(delivery.getDeliveryStatus()).isEqualTo(DeliveryStatus.COMPLETED);
                softly.assertThat(delivery.getArrivedAt()).isNotNull();
            });
        }

        @Test
        @DisplayName("결제 되지 않은 상태 -> 예외")
        void shouldFail_whenNotPaid() {
            //given
            Order order = orderService.createOrder(createCommand1);

            //when-then
            assertThatThrownBy(() -> orderService.completeDelivery(order.getId(), arrivedAt))
                    .isInstanceOf(DeliveryException.class);
        }

        @Test
        @DisplayName("배송 준비 되지 않은 상태 -> 예외")
        void shouldFail_whenNotPrepared() {
            //given
            Long orderId = createOrderAndProcessPayment(createCommand1);

            //when-then
            assertThatThrownBy(() -> orderService.completeDelivery(orderId, arrivedAt))
                    .isInstanceOf(DeliveryException.class);
        }

        @Test
        @DisplayName("배송 시작하지 않은 상태 -> 예외")
        void shouldFail_whenNotShipping() {
            //given
            Long orderId = createOrderAndProcessPayment(createCommand1);
            prepareDelivery(orderId);

            //when-then
            assertThatThrownBy(() -> orderService.completeDelivery(orderId, arrivedAt))
                    .isInstanceOf(DeliveryStatusException.class);
        }

        private void startDelivery(Long orderId) {
            orderService.startDelivery(orderId, "12345", arrivedAt);
        }

        private @NotNull Long prepareDelivery(Long orderId) {
            Address shipAddr = createSampleAddr();
            orderService.prepareDelivery(orderId, shipAddr);
            return orderId;
        }
    }

    private Long createOrderAndProcessPayment(OrderCreateCommand command) {
        Order order = orderService.createOrder(command);
        orderService.processPayment(order.getId(), new PayCommand(PayMethod.CARD, order.getOrderAmount().getFinalAmount()));
        return order.getId();
    }

    private Address createSampleAddr() {
        return Address.createAddress(
                "10580",
                "서울특별시",
                "노원구",
                "노원동",
                "상가 1층"
        );
    }
}
