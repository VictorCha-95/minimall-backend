    package com.minimall.domain.order;

    import com.minimall.domain.common.DomainType;
    import com.minimall.domain.embeddable.Address;
    import com.minimall.domain.member.Member;
    import com.minimall.domain.order.delivery.DeliveryStatus;
    import com.minimall.domain.order.delivery.DeliveryStatusException;
    import com.minimall.domain.order.exception.OrderStatusException;
    import com.minimall.domain.order.pay.PayMethod;
    import com.minimall.domain.order.pay.PayStatus;
    import com.minimall.domain.product.Product;
    import org.junit.jupiter.api.*;

    import java.time.LocalDateTime;
    import java.util.ArrayList;
    import java.util.List;

    import static com.minimall.domain.order.OrderStatus.*;
    import static org.assertj.core.api.Assertions.assertThat;
    import static org.assertj.core.api.Assertions.assertThatThrownBy;
    import static org.assertj.core.api.SoftAssertions.*;

    @DisplayName("Order.cancel()")
    public class OrderCancelTest {

        Member member;

        List<OrderItem> orderItems = new ArrayList<>();

        Order order;

        Product keyboard;
        Product mouse;

        final int keyboardPrice = 50_000;
        final int keyboardStock = 50;
        final int keyboardOrderQuantity = 30;

        final int mousePrice = 20_000;
        final int mouseStock = 20;
        final int mouseOrderQuantity = 10;

        final String trackingNo = "12345678";
        final LocalDateTime shippedAt = LocalDateTime.of(2025, 11, 10, 12, 30);
        final LocalDateTime arrivedAt = LocalDateTime.of(2025, 11, 20, 12, 30);

        private static final String DEFAULT_LOGIN_ID = "user123";
        private static final String DEFAULT_PASSWORD_HASH = "12345678";
        private static final String DEFAULT_NAME = "차태승";
        private static final String DEFAULT_EMAIL = "user123@example.com";
        private static final Address DEFAULT_ADDRESS =
                Address.createAddress("62550", "광주광역시", "광산구", "수등로76번길 40", "123동 456호");

        @BeforeEach
        void setUp() {
            member = Member.registerCustomer(
                    DEFAULT_LOGIN_ID, DEFAULT_PASSWORD_HASH, DEFAULT_NAME, DEFAULT_EMAIL, DEFAULT_ADDRESS
            );

            //== Product Entity ==//
            keyboard = new Product("키보드", keyboardPrice, keyboardStock);
            mouse = new Product("마우스", mousePrice, mouseStock);

            orderItems.add(OrderItem.createOrderItem(keyboard, keyboardOrderQuantity));
            orderItems.add(OrderItem.createOrderItem(mouse, mouseOrderQuantity));

            order = Order.createOrder(member, orderItems.toArray(OrderItem[]::new));
        }

        //== Helper Methods ==//
        private int total() {
            return (keyboardPrice * keyboardOrderQuantity) + (mousePrice * mouseOrderQuantity);
        }

        private void pay() {
            order.processPayment(new Pay(PayMethod.MOBILE_PAY, total()));
        }

        private void prepareDelivery() {
            order.prepareDelivery(member.getAddr());
        }

        private void startDelivery() {
            order.startDelivery(trackingNo, shippedAt);
        }

        private void completeDelivery() {
            order.completeDelivery(arrivedAt);
        }

        //== Test Groups ==//
        @Nested
        @DisplayName("결제 전")
        class Before_Payment {

            @Test
            @DisplayName("취소 성공 -> 주문 상태 변경, 재고 복원")
            void success_changeOrderStatus_restoreStock() {
                //when
                order.cancel();

                //then
                assertSoftly(softly -> {
                    softly.assertThat(order.getPay()).isNull();
                    softly.assertThat(order.getDelivery()).isNull();
                    softly.assertThat(order.getOrderStatus()).isEqualTo(CANCELED);
                    softly.assertThat(keyboard.getStockQuantity()).isEqualTo(keyboardStock);
                    softly.assertThat(mouse.getStockQuantity()).isEqualTo(mouseStock);
                    softly.assertThat(order.getOrderItems()).hasSize(2);
                });
            }
        }

        @Nested
        @DisplayName("결제 완료 / 배송 전")
        class After_Payment_Before_Delivery {
            @Test
            @DisplayName("취소 성공 -> 결제/주문 상태 변경, 재고 복원")
            void success_changePayAndOrderStatus_restoreStock() {
                //when
                Pay pay = new Pay(PayMethod.MOBILE_PAY, total());
                order.processPayment(pay);
                order.cancel();

                //then
                assertSoftly(softly -> {
                    softly.assertThat(pay.getPayStatus()).isEqualTo(PayStatus.CANCELED);
                    softly.assertThat(order.getOrderStatus()).isEqualTo(CANCELED);
                    softly.assertThat(order.getDelivery()).isNull();
                    softly.assertThat(keyboard.getStockQuantity()).isEqualTo(keyboardStock); // keyboardStock - keyboardOrderQuantity -> keyboardStock
                    softly.assertThat(mouse.getStockQuantity()).isEqualTo(mouseStock); //mouseStock - mouseOrderQuantity -> mouseStock
                });

            }
        }

        @Nested
        @DisplayName("결제 완료 / 배송 준비 중")
        class After_Payment_Preparing_Delivery{
            @Test
            @DisplayName("취소 성공 -> 배송/결제/주문 상태 변경, 재고 복원")
            void success_changeDeliveryAndPayAndOrderStatus_restoreStock() {
                //given
                Pay pay = new Pay(PayMethod.MOBILE_PAY, total());
                order.processPayment(pay);
                prepareDelivery();

                //when
                order.cancel();

                //then
                assertSoftly(softly -> {
                    softly.assertThat(pay.getPayStatus()).isEqualTo(PayStatus.CANCELED);
                    softly.assertThat(order.getDelivery().getDeliveryStatus()).isEqualTo(DeliveryStatus.CANCELED);
                    softly.assertThat(order.getOrderStatus()).isEqualTo(CANCELED);
                    softly.assertThat(keyboard.getStockQuantity()).isEqualTo(keyboardStock);
                    softly.assertThat(mouse.getStockQuantity()).isEqualTo(mouseStock);

                });
            }
        }

        @Nested
        @DisplayName("배송 중")
        class Shipping_In_Progress{
            @Test
            @DisplayName("취소 불가 -> 예외")
            void shouldFail() {
                //given
                pay();
                prepareDelivery();
                startDelivery();

                //when, then
                assertThatThrownBy(order::cancel)
                        .isInstanceOfSatisfying(DeliveryStatusException.class, e -> {
                            assertThat(e.getDomain()).isEqualTo(DomainType.DELIVERY);
                            assertThat(e.getCurrentStatus()).isEqualTo(DeliveryStatus.SHIPPING);
                            assertThat(e.getTargetStatus()).isEqualTo(DeliveryStatus.CANCELED);
                        });
            }
        }

        @Nested
        @DisplayName("배송 완료")
        class Shipping_Complete{
            @Test
            @DisplayName("취소 불가 -> 예외")
            void shouldFail() {
                //given
                pay();
                prepareDelivery();
                startDelivery();
                completeDelivery();

                //when, then
                assertThatThrownBy(order::cancel)
                        .isInstanceOfSatisfying(OrderStatusException.class, e -> {
                            assertThat(e.getDomain()).isEqualTo(DomainType.ORDER);
                            assertThat(e.getCurrentStatus()).isEqualTo(COMPLETED);
                            assertThat(e.getTargetStatus()).isEqualTo(CANCELED);
                        });
            }
        }

        @Nested
        @DisplayName("이미 취소된 주문")
        class Already_Canceled {
            @Test
            @DisplayName("재취소 불가 -> 예외")
            void cancel_shouldFail() {
                //given
                order.cancel();

                //when, then
                assertThatThrownBy(order::cancel)
                        .isInstanceOfSatisfying(OrderStatusException.class, e -> {
                            assertThat(e.getDomain()).isEqualTo(DomainType.ORDER);
                            assertThat(e.getCurrentStatus()).isEqualTo(CANCELED);
                            assertThat(e.getTargetStatus()).isEqualTo(CANCELED);
                        });
            }
        }
    }