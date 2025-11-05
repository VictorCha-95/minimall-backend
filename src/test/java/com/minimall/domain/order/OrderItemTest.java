package com.minimall.domain.order;

import com.minimall.domain.exception.DomainExceptionMessage;
import com.minimall.domain.order.exception.InvalidOrderItemException;
import com.minimall.domain.product.InvalidProductStockException;
import com.minimall.domain.product.Product;
import org.junit.jupiter.api.*;

import static org.assertj.core.api.Assertions.*;
import static org.assertj.core.api.SoftAssertions.*;

public class OrderItemTest {

    Product keyboard;

    int keyboardPrice = 50000;
    int keyboardStock = 50;
    int keyboardOrderQuantity = 30;

    @BeforeEach
    void setUp() {
        keyboard = new Product("키보드", keyboardPrice, keyboardStock);
    }

    @Nested
    class Create {
        @Test
        @DisplayName("정상 -> 생성")
        void success() {
            //when
            OrderItem orderItem = OrderItem.createOrderItem(keyboard, keyboardOrderQuantity);

            //then
            assertSoftly(softly -> {
                softly.assertThat(orderItem.getProduct()).isEqualTo(keyboard);
                softly.assertThat(orderItem.getProductName()).isEqualTo(keyboard.getName());
                softly.assertThat(orderItem.getOrderPrice()).isEqualTo(keyboardPrice);
                softly.assertThat(orderItem.getOrderQuantity()).isEqualTo(keyboardOrderQuantity);
            });
        }

        @Test
        @DisplayName("정상 -> 상품 재고 차감")
        void success_reduceStock() {
            //when
            OrderItem.createOrderItem(keyboard, keyboardOrderQuantity);

            //then
            assertThat(keyboard.getStockQuantity()).isEqualTo(keyboardStock - keyboardOrderQuantity);
        }

        @Test
        @DisplayName("주문 수량 > 재고 수량 -> 예외")
        void shouldFail_whenOrderQuantityGraterThanStockQuantity() {
            assertThatThrownBy(() -> OrderItem.createOrderItem(keyboard, keyboardStock + 1))
                    .isInstanceOf(InvalidProductStockException.class);

        }

        @Test
        @DisplayName("주문 항목 생성 후 상품명 변경 -> 주문 항목 상품명 불변")
        void notChanged_whenProductNameIsChanged() {
            //when
            OrderItem orderItem = OrderItem.createOrderItem(keyboard, keyboardOrderQuantity);
            keyboard.changeName("새로운 이름");

            //then
            assertThat(orderItem.getProductName()).isEqualTo("키보드");
        }

        @Test
        @DisplayName("주문 항목 생성 후 가격 변경 -> 주문 항목 가격 불변")
        void notChanged_whenProductPriceIsChanged() {
            //when
            OrderItem orderItem = OrderItem.createOrderItem(keyboard, keyboardOrderQuantity);
            keyboard.changePrice(9999999);

            //then
            assertThat(orderItem.getOrderPrice()).isEqualTo(keyboardPrice);
        }

        @Test
        @DisplayName("상품 null -> 예외")
        void shouldFail_whenProductIsNull() {
            assertThatThrownBy(() -> OrderItem.createOrderItem(null, keyboardOrderQuantity))
                    .isInstanceOfSatisfying(NullPointerException.class, e ->
                            assertThat(e.getMessage())
                                    .isEqualTo(DomainExceptionMessage.PARAM_REQUIRE_NOT_NULL.text("product")));
        }

        @Test
        @DisplayName("주문 수량 음수 -> 예외")
        void shouldFail_whenOrderQuantityIsNegative() {
            assertThatThrownBy(() -> OrderItem.createOrderItem(keyboard, -10))
                    .isInstanceOfSatisfying(InvalidOrderItemException.class, e -> {
                        assertThat(e.getReason()).isEqualTo(InvalidOrderItemException.Reason.NON_POSITIVE_QUANTITY);
                        assertThat(e.getRequestedQty()).isEqualTo(-10);
                    });
        }

        @Test
        @DisplayName("주문 수량 0 -> 예외")
        void shouldFail_whenOrderQuantityIsZero() {
            assertThatThrownBy(() -> OrderItem.createOrderItem(keyboard, 0))
                    .isInstanceOfSatisfying(InvalidOrderItemException.class, e -> {
                        assertThat(e.getReason()).isEqualTo(InvalidOrderItemException.Reason.NON_POSITIVE_QUANTITY);
                        assertThat(e.getRequestedQty()).isEqualTo(0);
                    });
        }
    }

    @Test
    @DisplayName("createTotalAmount()")
    void createTotalAmount() {
        //given
        OrderItem orderItem = OrderItem.createOrderItem(keyboard, keyboardOrderQuantity);

        //then
        assertThat(orderItem.getTotalAmount()).isEqualTo(keyboardPrice * keyboardOrderQuantity);
    }
}
