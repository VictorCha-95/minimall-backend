package com.minimall.domain.member;

import com.minimall.domain.embeddable.Address;
import com.minimall.domain.order.Order;
import com.minimall.domain.order.OrderItem;
import com.minimall.domain.product.Product;
import org.junit.jupiter.api.*;

import static org.assertj.core.api.Assertions.*;
import static org.assertj.core.api.SoftAssertions.*;

@DisplayName("Member 도메인")
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
public class MemberTest {

    Member member;

    @BeforeEach
    void setUp() {
        member = Member.builder()
                .loginId("user123")
                .password("12345")
                .email("user123@example.com")
                .name("차태승")
                .addr(Address.createAddress("62550", "광주광역시", "광산구", "수등로76번길 40", "123동 456호"))
                .build();
    }

    @Nested
    class Create {
        @Test
        @DisplayName("기본 등급 -> BRONZE")
        void originalGrade_Bronze() {
            //then
            assertThat(member.getGrade()).isEqualTo(Grade.BRONZE);
        }
    }

    @Nested
    class Update {
        @Test
        @DisplayName("PATCH -> null 값은 유지, 변경 값만 변경")
        void partial_update() {
            //when
            member.update(null, "손흥민", null, null);

            //then
            assertSoftly(softly -> {
                softly.assertThat(member.getName()).isEqualTo("손흥민");
                softly.assertThat(member.getEmail()).isEqualTo("user123@example.com");
            });
        }
    }

    @Nested
    class ChangePassword {
        @Test
        @DisplayName("비밀번호 교체")
        void change_password() {
            //when
            member.changePassword("p2");

            //then
            assertThat(member.getPassword()).isEqualTo("p2");
        }
    }

    @Nested
    class Upgrade_Grade {
        @Test
        @DisplayName("등급 승급")
        void upgrade_grade() {
            //when
            member.upgradeGrade(Grade.VIP);

            //then
            assertThat(member.getGrade()).isEqualTo(Grade.VIP);
        }
    }

    @Nested
    class Association {
        @Test
        @DisplayName("연관관계 무한루프 방지 -> 같은 주문 중복 추가 해도 1건으로 처리")
        void addOrder() {
            //when
            Order order = newOrder();
            member.addOrder(order);
            member.addOrder(order);

            //then
            assertThat(member.getOrders()).containsExactly(order);
            assertThat(order.getMember()).isEqualTo(member);


        }
    }

    private Order newOrder() {
        return Order.createOrder(member, OrderItem.createOrderItem(new Product("마우스", 20_000, 20), 10));
    }
}
