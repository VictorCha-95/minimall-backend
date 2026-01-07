package com.minimall.perf;

import static org.assertj.core.api.Assertions.*;

import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import com.minimall.AbstractIntegrationTest;
import com.minimall.domain.embeddable.Address;
import com.minimall.domain.member.Member;
import com.minimall.domain.member.MemberRepository;
import com.minimall.domain.product.Product;
import com.minimall.domain.product.ProductRepository;
import com.minimall.service.order.OrderService;
import com.minimall.service.order.dto.command.OrderCreateCommand;
import com.minimall.service.order.dto.command.OrderItemCreateCommand;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

// @Transactional 금지
@SpringBootTest
class StockConcurrencyReproduceTest {

    @Autowired OrderService orderService;
    @Autowired ProductRepository productRepository;
    @Autowired MemberRepository memberRepository;

    Member customer;

    private static final String DEFAULT_LOGIN_ID = "user123";
    private static final String DEFAULT_PASSWORD_HASH = "12345678";
    private static final String DEFAULT_NAME = "차태승";
    private static final String DEFAULT_EMAIL = "user123@example.com";
    private static final Address DEFAULT_ADDRESS =
            Address.createAddress("62550", "광주광역시", "광산구", "수등로76번길 40", "123동 456호");

    private static final int BEFORE_STOCK = 100;

    @BeforeEach
    void setUp() {
        customer = Member.registerCustomer(DEFAULT_LOGIN_ID, DEFAULT_PASSWORD_HASH, DEFAULT_NAME, DEFAULT_EMAIL, DEFAULT_ADDRESS);
    }

    @Test
    void 재고100_동시주문200건_오버셀_재현() throws Exception {
        // given: 재고 100짜리 상품 1개, 주문 가능한 회원 1명
        Member member = memberRepository.save(customer);
        Product product = productRepository.save(new Product("마우스", 20_000, BEFORE_STOCK));

        long memberId = member.getId();
        long productId = product.getId();

        int threads = 200;
        int poolSize = 32;

        ExecutorService pool = Executors.newFixedThreadPool(poolSize);

        CountDownLatch ready = new CountDownLatch(threads); // 모든 스레드 준비 완료 대기
        CountDownLatch start = new CountDownLatch(1);       // 동시에 출발시키는 신호
        CountDownLatch done  = new CountDownLatch(threads); // 종료 대기

        AtomicInteger success = new AtomicInteger();
        AtomicInteger fail = new AtomicInteger();

        // when: "주문 1건 당 재고 1건 감소"
        for (int i = 0; i < threads; i++) {
            pool.submit(() -> {
                ready.countDown();
                try {
                    start.await();

                    orderService.createOrder(new OrderCreateCommand(memberId,
                            List.of(new OrderItemCreateCommand(productId, 1))));

                    success.incrementAndGet();
                } catch (Exception e) {
                    fail.incrementAndGet();
                } finally {
                    done.countDown();
                }
            });
        }

        // 모든 스레드가 준비될 때까지 기다렸다가 동시에 시작
        ready.await();
        start.countDown();
        done.await();

        pool.shutdown();

        // then: 개선 전에는 보통 여기서 정합성 깨짐이 관측되어야 "실패 재현" 성공
        Product reloaded = productRepository.findById(productId).orElseThrow();

        int finalStock = reloaded.getStockQuantity();

        System.out.println("success=" + success.get() + ", fail=" + fail.get() + ", finalStock=" + finalStock);

        // 실패 재현의 핵심(둘 중 하나라도 터지면 동시성 이슈가 "증명"됨)
        // 1) 성공 주문이 초기재고(100)보다 많다 = 오버셀
        // 2) 재고가 음수가 된다
        assertThat(success.get() > BEFORE_STOCK || finalStock < 0)
                .as("개선 전에는 오버셀 또는 재고 음수가 재현되어야 함")
                .isTrue();
    }
}
