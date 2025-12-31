package com.minimall.domain.member;

import com.minimall.domain.embeddable.Address;
import com.minimall.domain.exception.DomainExceptionMessage;
import com.minimall.domain.member.exception.InvalidEmailException;
import com.minimall.domain.member.exception.InvalidLoginIdException;
import com.minimall.domain.member.exception.InvalidMemberNameException;
import com.minimall.domain.member.exception.InvalidPasswordException;
import com.minimall.domain.order.Order;
import com.minimall.domain.order.OrderItem;
import com.minimall.domain.product.Product;
import org.junit.jupiter.api.*;

import static org.assertj.core.api.Assertions.*;
import static org.assertj.core.api.SoftAssertions.*;

@DisplayName("Member 도메인")
public class MemberTest {

    Member customer;

    private static final String DEFAULT_LOGIN_ID = "user123";
    private static final String DEFAULT_PASSWORD_HASH = "12345678";
    private static final String DEFAULT_NAME = "차태승";
    private static final String DEFAULT_EMAIL = "user123@example.com";
    private static final Address DEFAULT_ADDRESS =
            Address.createAddress("62550", "광주광역시", "광산구", "수등로76번길 40", "123동 456호");

    @BeforeEach
    void setUp() {
        customer = Member.registerCustomer(DEFAULT_LOGIN_ID, DEFAULT_PASSWORD_HASH, DEFAULT_NAME, DEFAULT_EMAIL, DEFAULT_ADDRESS);
    }

    @Nested
    class Create {
        @Test
        @DisplayName("고객 등록 -> 필드 검증")
        void registerCustomer_success() {
            assertSoftly(softly -> {
                softly.assertThat(customer.getLoginId()).isEqualTo(DEFAULT_LOGIN_ID);
                softly.assertThat(customer.getPasswordHash()).isEqualTo(DEFAULT_PASSWORD_HASH);
                softly.assertThat(customer.getEmail()).isEqualTo(DEFAULT_EMAIL);
                softly.assertThat(customer.getName()).isEqualTo(DEFAULT_NAME);
                softly.assertThat(customer.getAddr()).isNotNull();
                softly.assertThat(customer.getRole()).isEqualTo(Role.CUSTOMER);
                softly.assertThat(customer.getCustomerProfile().getGrade()).isEqualTo(CustomerGrade.BRONZE);
            });
        }

        @Test
        @DisplayName("판매자 등록 -> Role 검증")
        void registerSeller_success() {
            Member seller = Member.registerSeller("Seller", "SellerHash", "Seller123",
                    "seller@naver.com", null, "store", "12345-12345",
                    "농협/12345/seller");

            assertThat(seller.getRole()).isEqualTo(Role.SELLER);

        }


        @Test
        @DisplayName("관리자 등록 -> Role 검증")
        void registerAdmin_success(){
            Member admin = Member.registerAdmin(DEFAULT_LOGIN_ID, DEFAULT_PASSWORD_HASH, DEFAULT_NAME, DEFAULT_EMAIL, DEFAULT_ADDRESS);

            assertThat(admin.getRole()).isEqualTo(Role.ADMIN);
        }

        @Nested
        class LoginId {
            @Test
            @DisplayName("null -> 예외")
            void shouldFail_whenLoginIdIsNull() {
                assertThatThrownBy(() -> Member.registerCustomer(null, DEFAULT_PASSWORD_HASH, DEFAULT_NAME, DEFAULT_EMAIL, DEFAULT_ADDRESS))
                        .isInstanceOfSatisfying(InvalidLoginIdException.class, e -> {
                            assertThat(e.getReason()).isEqualTo(InvalidLoginIdException.Reason.REQUIRED);
                            assertThat(e.getMessage())
                                    .isEqualTo(DomainExceptionMessage.PARAM_REQUIRE_NOT_NULL.text(Fields.MEMBER_LOGIN_ID));
                        });
            }

            @Test
            @DisplayName("blank -> 예외")
            void shouldFail_whenLoginIdIsBlank() {
                assertThatThrownBy(() -> Member.registerCustomer("  ", DEFAULT_PASSWORD_HASH, DEFAULT_NAME, DEFAULT_EMAIL, DEFAULT_ADDRESS))
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
                assertThatThrownBy(() -> Member.registerCustomer(DEFAULT_LOGIN_ID, null, DEFAULT_NAME, DEFAULT_EMAIL, DEFAULT_ADDRESS))
                        .isInstanceOfSatisfying(InvalidPasswordException.class, e -> {
                            assertThat(e.getReason()).isEqualTo(InvalidPasswordException.Reason.REQUIRED);
                            assertThat(e.getMessage())
                                    .isEqualTo(DomainExceptionMessage.PARAM_REQUIRE_NOT_NULL.text(Fields.MEMBER_PASSWORD));
                        });
            }

            @Test
            @DisplayName("blank -> 예외")
            void shouldFail_whenPasswordIsBlank() {
                assertThatThrownBy(() -> Member.registerCustomer(DEFAULT_LOGIN_ID, "  ", DEFAULT_NAME, DEFAULT_EMAIL, DEFAULT_ADDRESS))
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
                assertThatThrownBy(() -> Member.registerCustomer(DEFAULT_LOGIN_ID, DEFAULT_PASSWORD_HASH, null, DEFAULT_EMAIL, DEFAULT_ADDRESS))
                        .isInstanceOfSatisfying(InvalidMemberNameException.class, e -> {
                            assertThat(e.getReason()).isEqualTo(InvalidMemberNameException.Reason.REQUIRED);
                            assertThat(e.getMessage())
                                    .isEqualTo(DomainExceptionMessage.PARAM_REQUIRE_NOT_NULL.text(Fields.MEMBER_NAME));
                        });
            }

            @Test
            @DisplayName("blank -> 예외")
            void shouldFail_whenNameIsBlank() {
                assertThatThrownBy(() -> Member.registerCustomer(DEFAULT_LOGIN_ID, DEFAULT_PASSWORD_HASH, "  ", DEFAULT_EMAIL, DEFAULT_ADDRESS))
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
                assertThatThrownBy(() -> Member.registerCustomer(DEFAULT_LOGIN_ID, DEFAULT_PASSWORD_HASH, DEFAULT_NAME, null, DEFAULT_ADDRESS))
                        .isInstanceOfSatisfying(InvalidEmailException.class, e -> {
                            assertThat(e.getReason()).isEqualTo(InvalidEmailException.Reason.REQUIRED);
                            assertThat(e.getMessage())
                                    .isEqualTo(DomainExceptionMessage.PARAM_REQUIRE_NOT_NULL.text(Fields.MEMBER_EMAIL));
                        });
            }

            @Test
            @DisplayName("blank -> 예외")
            void shouldFail_whenEmailIsBlank() {
                assertThatThrownBy(() -> Member.registerCustomer(DEFAULT_LOGIN_ID, DEFAULT_PASSWORD_HASH, DEFAULT_NAME, "  ", DEFAULT_ADDRESS))
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
            customer.update(null, "손흥민", null, null);

            //then
            assertSoftly(softly -> {
                softly.assertThat(customer.getName()).isEqualTo("손흥민");
                softly.assertThat(customer.getPasswordHash()).isEqualTo(DEFAULT_PASSWORD_HASH);
                softly.assertThat(customer.getEmail()).isEqualTo(DEFAULT_EMAIL);
            });
        }
    }

    @Nested
    class ChangePassword {
        @Test
        @DisplayName("정상 -> 변경")
        void change_password() {
            //when
            customer.changePassword("p2");

            //then
            assertThat(customer.getPasswordHash()).isEqualTo("p2");
        }
    }

    @Nested
    class ChangeGrade {
        @Test
        @DisplayName("정상 -> 변경")
        void upgrade_grade() {
            //when
            customer.getCustomerProfile().changeGrade(CustomerGrade.VIP);

            //then
            assertThat(customer.getCustomerProfile().getGrade()).isEqualTo(CustomerGrade.VIP);
        }
    }

    @Nested
    class Association {
        @Test
        @DisplayName("addOrder -> 연관관계 추가")
        void success_addOrder() {
            //when
            Order order = newOrder();
            customer.addOrder(order);

            //then
            assertThat(customer.getOrders()).containsExactly(order);
            assertThat(order.getMember()).isEqualTo(customer);
        }

        @Test
        @DisplayName("같은 주문 반복 addOrder -> 1건만 추가")
        void onlyOneProcess_whenAddDuplicatedOrder() {
            //when
            Order order = newOrder();
            customer.addOrder(order);
            customer.addOrder(order);

            //then
            assertThat(customer.getOrders()).containsExactly(order);
            assertThat(order.getMember()).isEqualTo(customer);
        }
    }

    private Order newOrder() {
        return Order.createOrder(customer, OrderItem.createOrderItem(new Product("마우스", 20_000, 20), 10));
    }
}
