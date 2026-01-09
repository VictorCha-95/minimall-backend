package com.minimall.service.order;

import com.minimall.AbstractIntegrationTest;
import com.minimall.domain.embeddable.Address;
import com.minimall.domain.member.Member;
import com.minimall.domain.member.MemberRepository;
import com.minimall.domain.order.pay.PayMethod;
import com.minimall.domain.product.Product;
import com.minimall.domain.product.ProductRepository;
import com.minimall.service.order.dto.command.OrderCreateCommand;
import com.minimall.service.order.dto.command.OrderItemCreateCommand;
import com.minimall.service.order.dto.command.PayCommand;
import com.minimall.service.order.dto.result.OrderCreateResult;
import com.minimall.service.order.dto.result.OrderSummaryResult;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class OrderQueryCountIntegrationTest extends AbstractIntegrationTest {

    private static final int ORDER_COUNT = 20;
    private static final int ITEMS_PER_ORDER = 3;

    @Autowired
    OrderService orderService;

    @Autowired
    MemberRepository memberRepository;

    @Autowired
    ProductRepository productRepository;

    @Autowired
    EntityManager em;

    @Autowired
    EntityManagerFactory emf;

    private Statistics statistics;

    @BeforeEach
    void setUpStatistics() {
        SessionFactory sessionFactory = emf.unwrap(SessionFactory.class);
        statistics = sessionFactory.getStatistics();
        statistics.setStatisticsEnabled(true);
        statistics.clear();
    }

    @Test
    @DisplayName("주문 목록 조회 쿼리 수 카운트: N+1 감지")
    void shouldCountQueriesForOrderSummaries() {
        // given
        Address address = Address.createAddress("12345", "State", "City", "Street", "Detail");
        Member member = memberRepository.save(
                Member.registerCustomer(unique("login"), "password", "name", unique("email") + "@test.com", address)
        );

        List<Product> products = List.of(
                new Product("product-1", 1_000, 1_000),
                new Product("product-2", 2_000, 1_000),
                new Product("product-3", 3_000, 1_000)
        );
        products.forEach(productRepository::save);

        List<OrderItemCreateCommand> itemCommands = products.stream()
                .limit(ITEMS_PER_ORDER)
                .map(product -> new OrderItemCreateCommand(product.getId(), 1))
                .toList();

        for (int i = 0; i < ORDER_COUNT; i++) {
            OrderCreateCommand command = new OrderCreateCommand(member.getId(), itemCommands);
            OrderCreateResult order = orderService.createOrder(command);
            orderService.processPayment(order.id(),
                    new PayCommand(PayMethod.CARD, order.finalAmount()));
            orderService.prepareDelivery(order.id(), address);
        }

        flushAndClear();
        statistics.clear();

        // when
        List<OrderSummaryResult> result = orderService.getOrderSummaries(member.getId());

        // then
        long queryCount = statistics.getPrepareStatementCount();
        System.out.printf("Order summaries query count: %d%n", queryCount);

        assertThat(result).hasSize(ORDER_COUNT);
        int threshold = 8; // 20건 조회 시 정상 구현은 2~3쿼리 수준, N+1이면 20건 이상으로 증가
        assertThat(queryCount).isLessThanOrEqualTo(threshold);
    }

    private void flushAndClear() {
        em.flush();
        em.clear();
    }

    private static String unique(String prefix) {
        return prefix + "-" + UUID.randomUUID();
    }
}
