package com.minimall.domain.member;

import com.minimall.domain.embeddable.Address;
import com.minimall.domain.embeddable.InvalidAddressException;
import com.minimall.domain.exception.DomainExceptionMessage;
import com.minimall.domain.order.Order;
import com.minimall.domain.order.OrderItem;
import com.minimall.domain.product.Product;
import org.junit.jupiter.api.*;

import static org.assertj.core.api.Assertions.*;
import static org.assertj.core.api.SoftAssertions.*;

@DisplayName("Member 도메인")
public class MemberTest {

    Member member;

    private static final String DEFAULT_LOGIN_ID = "user123";
    private static final String DEFAULT_PASSWORD = "12345678";
    private static final String DEFAULT_NAME = "차태승";
    private static final String DEFAULT_EMAIL = "user123@example.com";
    private static final Address DEFAULT_ADDRESS =
            Address.createAddress("62550", "광주광역시", "광산구", "수등로76번길 40", "123동 456호");

    @BeforeEach
    void setUp() {
        member = Member.builder()
                .loginId(DEFAULT_LOGIN_ID)
                .password(DEFAULT_PASSWORD)
                .name(DEFAULT_NAME)
                .email(DEFAULT_EMAIL)
                .addr(DEFAULT_ADDRESS)
                .build();
    }

    @Nested
    class Create {
        @Test
        @DisplayName("정상 -> 생성(기본 등급: 브론즈)")
        void success() {
            assertSoftly(softly -> {
                softly.assertThat(member.getLoginId()).isEqualTo(DEFAULT_LOGIN_ID);
                softly.assertThat(member.getPassword()).isEqualTo(DEFAULT_PASSWORD);
                softly.assertThat(member.getEmail()).isEqualTo(DEFAULT_EMAIL);
                softly.assertThat(member.getName()).isEqualTo(DEFAULT_NAME);
                softly.assertThat(member.getAddr()).isNotNull();
                softly.assertThat(member.getGrade()).isEqualTo(Grade.BRONZE);
            });
        }

        @Nested
        class LoginId {
            @Test
            @DisplayName("null -> 예외")
            void shouldFail_whenLoginIdIsNull() {
                assertThatThrownBy(() -> Member.builder()
                        .loginId(null)
                        .password(DEFAULT_PASSWORD)
                        .name(DEFAULT_NAME)
                        .email(DEFAULT_EMAIL)
                        .addr(DEFAULT_ADDRESS)
                        .build())
                        .isInstanceOfSatisfying(InvalidLoginIdException.class, e -> {
                            assertThat(e.getReason()).isEqualTo(InvalidLoginIdException.Reason.REQUIRED);
                            assertThat(e.getMessage())
                                    .isEqualTo(DomainExceptionMessage.PARAM_REQUIRE_NOT_NULL.text(Fields.MEMBER_LOGIN_ID));
                        });
            }

            @Test
            @DisplayName("blank -> 예외")
            void shouldFail_whenLoginIdIsBlank() {
                assertThatThrownBy(() -> Member.builder()
                        .loginId("    ")
                        .password(DEFAULT_PASSWORD)
                        .name(DEFAULT_NAME)
                        .email(DEFAULT_EMAIL)
                        .addr(DEFAULT_ADDRESS)
                        .build())
                        .isInstanceOfSatisfying(InvalidLoginIdException.class, e -> {
                            assertThat(e.getReason()).isEqualTo(InvalidLoginIdException.Reason.BLANK);
                            assertThat(e.getMessage())
                                    .isEqualTo(DomainExceptionMessage.PARAM_REQUIRE_NOT_BLANK.text(Fields.MEMBER_LOGIN_ID));
                        });
            }

        }

        @Nested
        class Password {
            @Test
            @DisplayName("null -> 예외")
            void shouldFail_whenPasswordIsNull() {
                assertThatThrownBy(() -> Member.builder()
                        .loginId(DEFAULT_LOGIN_ID)
                        .password(null)
                        .name(DEFAULT_NAME)
                        .email(DEFAULT_EMAIL)
                        .addr(DEFAULT_ADDRESS)
                        .build())
                        .isInstanceOfSatisfying(InvalidPasswordException.class, e -> {
                            assertThat(e.getReason()).isEqualTo(InvalidPasswordException.Reason.REQUIRED);
                            assertThat(e.getMessage())
                                    .isEqualTo(DomainExceptionMessage.PARAM_REQUIRE_NOT_NULL.text(Fields.MEMBER_PASSWORD));
                        });
            }

            @Test
            @DisplayName("blank -> 예외")
            void shouldFail_whenPasswordIsBlank() {
                assertThatThrownBy(() -> Member.builder()
                        .loginId(DEFAULT_LOGIN_ID)
                        .password("    ")
                        .name(DEFAULT_NAME)
                        .email(DEFAULT_EMAIL)
                        .addr(DEFAULT_ADDRESS)
                        .build())
                        .isInstanceOfSatisfying(InvalidPasswordException.class, e -> {
                            assertThat(e.getReason()).isEqualTo(InvalidPasswordException.Reason.BLANK);
                            assertThat(e.getMessage())
                                    .isEqualTo(DomainExceptionMessage.PARAM_REQUIRE_NOT_BLANK.text(Fields.MEMBER_PASSWORD));
                        });
            }
        }

        @Nested
        class Name {
            @Test
            @DisplayName("null -> 예외")
            void shouldFail_whenNameIsNull() {
                assertThatThrownBy(() -> Member.builder()
                        .loginId(DEFAULT_LOGIN_ID)
                        .password(DEFAULT_PASSWORD)
                        .name(null)
                        .email(DEFAULT_EMAIL)
                        .addr(DEFAULT_ADDRESS)
                        .build())
                        .isInstanceOfSatisfying(InvalidMemberNameException.class, e -> {
                            assertThat(e.getReason()).isEqualTo(InvalidMemberNameException.Reason.REQUIRED);
                            assertThat(e.getMessage())
                                    .isEqualTo(DomainExceptionMessage.PARAM_REQUIRE_NOT_NULL.text(Fields.MEMBER_NAME));
                        });
            }

            @Test
            @DisplayName("blank -> 예외")
            void shouldFail_whenNameIsBlank() {
                assertThatThrownBy(() -> Member.builder()
                        .loginId(DEFAULT_LOGIN_ID)
                        .password(DEFAULT_PASSWORD)
                        .name("     ")
                        .email(DEFAULT_EMAIL)
                        .addr(DEFAULT_ADDRESS)
                        .build())
                        .isInstanceOfSatisfying(InvalidMemberNameException.class, e -> {
                            assertThat(e.getReason()).isEqualTo(InvalidMemberNameException.Reason.BLANK);
                            assertThat(e.getMessage())
                                    .isEqualTo(DomainExceptionMessage.PARAM_REQUIRE_NOT_BLANK.text(Fields.MEMBER_NAME));
                        });
            }
        }

        @Nested
        class Email {
            @Test
            @DisplayName("null -> 예외")
            void shouldFail_whenEmailIsNull() {
                assertThatThrownBy(() -> Member.builder()
                        .loginId(DEFAULT_LOGIN_ID)
                        .password(DEFAULT_PASSWORD)
                        .name(DEFAULT_NAME)
                        .email(null)
                        .addr(DEFAULT_ADDRESS)
                        .build())
                        .isInstanceOfSatisfying(InvalidEmailException.class, e -> {
                            assertThat(e.getReason()).isEqualTo(InvalidEmailException.Reason.REQUIRED);
                            assertThat(e.getMessage())
                                    .isEqualTo(DomainExceptionMessage.PARAM_REQUIRE_NOT_NULL.text(Fields.MEMBER_EMAIL));
                        });
            }

            @Test
            @DisplayName("blank -> 예외")
            void shouldFail_whenEmailIsBlank() {
                assertThatThrownBy(() -> Member.builder()
                        .loginId(DEFAULT_LOGIN_ID)
                        .password(DEFAULT_PASSWORD)
                        .name(DEFAULT_NAME)
                        .email("   ")
                        .addr(DEFAULT_ADDRESS)
                        .build())
                        .isInstanceOfSatisfying(InvalidEmailException.class, e -> {
                            assertThat(e.getReason()).isEqualTo(InvalidEmailException.Reason.BLANK);
                            assertThat(e.getMessage())
                                    .isEqualTo(DomainExceptionMessage.PARAM_REQUIRE_NOT_BLANK.text(Fields.MEMBER_EMAIL));
                        });
            }
        }
    }

    @Nested
    class Update {
        @Test
        @DisplayName("부분 변경 -> 기존 값 유지")
        void partial_update() {
            //when
            member.update(null, "손흥민", null, null);

            //then
            assertSoftly(softly -> {
                softly.assertThat(member.getName()).isEqualTo("손흥민");
                softly.assertThat(member.getPassword()).isEqualTo(DEFAULT_PASSWORD);
                softly.assertThat(member.getEmail()).isEqualTo(DEFAULT_EMAIL);
            });
        }
    }

    @Nested
    class ChangePassword {
        @Test
        @DisplayName("정상 -> 변경")
        void change_password() {
            //when
            member.changePassword("p2");

            //then
            assertThat(member.getPassword()).isEqualTo("p2");
        }
    }

    @Nested
    class ChangeGrade {
        @Test
        @DisplayName("정상 -> 변경")
        void upgrade_grade() {
            //when
            member.changeGrade(Grade.VIP);

            //then
            assertThat(member.getGrade()).isEqualTo(Grade.VIP);
        }
    }

    @Nested
    class Association {
        @Test
        @DisplayName("addOrder -> 연관관계 추가")
        void success_addOrder() {
            //when
            Order order = newOrder();
            member.addOrder(order);

            //then
            assertThat(member.getOrders()).containsExactly(order);
            assertThat(order.getMember()).isEqualTo(member);
        }

        @Test
        @DisplayName("같은 주문 반복 addOrder -> 1건만 추가")
        void onlyOneProcess_whenAddDuplicatedOrder() {
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
