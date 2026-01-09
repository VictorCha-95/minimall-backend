package com.minimall.service.order;

import com.minimall.AbstractIntegrationTest;
import com.minimall.domain.member.Member;
import com.minimall.domain.member.MemberRepository;
import com.minimall.domain.product.Product;
import com.minimall.domain.product.ProductRepository;
import com.minimall.fixture.MemberFixture;
import com.minimall.fixture.OrderFixture;
import com.minimall.fixture.ProductFixture;
import com.minimall.service.order.dto.command.OrderCreateCommand;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.LongAdder;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
public class StockConcurrencyIntegrationTest extends AbstractIntegrationTest {

    private static final Logger log = LoggerFactory.getLogger(StockConcurrencyIntegrationTest.class);

    private static final int INITIAL_STOCK = 100;
    private static final int REQUEST_COUNT = 200;
    private static final int ORDER_QUANTITY = 1;
    private static final int THREAD_POOL_SIZE = 32;

    @Autowired
    OrderService orderService;

    @Autowired
    MemberRepository memberRepository;

    @Autowired
    ProductRepository productRepository;

    @Autowired
    EntityManager em;

    @Test
    @DisplayName("재고 동시성: 락 미적용 시 오버셀 재현")
    void shouldOversellWithoutLock() throws InterruptedException {
        // Given
        Member member = MemberFixture.createMemberSaved(memberRepository, "concurrency-user", "동시성회원");
        Product product = ProductFixture.createProductSaved(
                productRepository,
                "concurrency-product",
                10_000,
                INITIAL_STOCK
        );
        OrderCreateCommand command = OrderFixture.createOrderCreateCommand(
                member.getId(),
                product.getId(),
                ORDER_QUANTITY
        );

        ExecutorService executor = Executors.newFixedThreadPool(THREAD_POOL_SIZE);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(REQUEST_COUNT);

        AtomicInteger successCount = new AtomicInteger();
        AtomicInteger failCount = new AtomicInteger();
        ConcurrentHashMap<Class<?>, LongAdder> exceptionCounts = new ConcurrentHashMap<>();

        // When
        for (int i = 0; i < REQUEST_COUNT; i++) {
            executor.submit(() -> {
                try {
                    startLatch.await();
                    orderService.createOrder(command);
                    successCount.incrementAndGet();
                } catch (Exception e) {
                    failCount.incrementAndGet();
                    exceptionCounts.computeIfAbsent(e.getClass(), key -> new LongAdder()).increment();
                } finally {
                    doneLatch.countDown();
                }
            });
        }

        startLatch.countDown();
        doneLatch.await();
        executor.shutdown();
        executor.awaitTermination(10, TimeUnit.SECONDS);

        em.clear();
        Integer finalStock = productRepository.findById(product.getId())
                .orElseThrow()
                .getStockQuantity();

        Long totalOrderedQuantity = em.createQuery(
                        "select coalesce(sum(oi.orderQuantity), 0) from OrderItem oi where oi.product.id = :productId",
                        Long.class)
                .setParameter("productId", product.getId())
                .getSingleResult();

        Map<String, Integer> exceptionSummary = exceptionCounts.entrySet().stream()
                .collect(Collectors.toMap(
                        entry -> entry.getKey().getSimpleName(),
                        entry -> entry.getValue().intValue()
                ));

        // Then
        log.info(
                "initialStock={}, requestCount={}, successCount={}, failCount={}, finalStock={}, exceptionSummary={}",
                INITIAL_STOCK, REQUEST_COUNT, successCount.get(), failCount.get(), finalStock, exceptionSummary
        );

        assertThat(finalStock).isGreaterThanOrEqualTo(0);
        assertThat(successCount.get()).isLessThanOrEqualTo(INITIAL_STOCK);
        assertThat(totalOrderedQuantity).isLessThanOrEqualTo(INITIAL_STOCK);
    }

}
