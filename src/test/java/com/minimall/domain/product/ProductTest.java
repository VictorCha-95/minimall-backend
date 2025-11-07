package com.minimall.domain.product;

import com.minimall.domain.exception.DomainExceptionMessage;
import org.junit.jupiter.api.*;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;
import static org.assertj.core.api.SoftAssertions.*;


@DisplayName("Product 도메인")
public class ProductTest {

    private static final String DEFAULT_NAME = "마우스";
    private static final int DEFAULT_PRICE = 20_000;
    private static final int DEFAULT_STOCK = 50;
    private static final int DEFAULT_NEGATIVE = -100;

    private Map<String, String> paramMap = new HashMap<>();

    @BeforeEach
    void setup() {
        paramMap.put("price", "product.price");
        paramMap.put("stock", "product.stock");
        paramMap.put("name", "product.name");
    }

    @Nested
    class Create {
        @Test
        @DisplayName("정상 -> 생성")
        void success() {
            //when
            Product product = new Product(DEFAULT_NAME, DEFAULT_PRICE, DEFAULT_STOCK);

            //then
            assertSoftly(softly -> {
                softly.assertThat(product.getName()).isEqualTo(DEFAULT_NAME);
                softly.assertThat(product.getPrice()).isEqualTo(DEFAULT_PRICE);
                softly.assertThat(product.getStockQuantity()).isEqualTo(DEFAULT_STOCK);
            });
        }

        @Nested
        class Name {
            @Test
            @DisplayName("null -> 예외")
            void shouldFail_whenNameIsNull() {
                assertThatThrownBy(() -> new Product(null, DEFAULT_PRICE, DEFAULT_STOCK))
                        .isInstanceOfSatisfying(InvalidProductNameException.class, e -> {
                            assertThat(e.getReason()).isEqualTo(InvalidProductNameException.Reason.REQUIRED);
                            assertThat(e.getMessage())
                                    .isEqualTo(DomainExceptionMessage.PARAM_REQUIRE_NOT_NULL.text(paramMap.get("name")));
                        });
            }

            @Test
            @DisplayName("공백 -> 예외")
            void shouldFail_whenNameIsBlank() {
                assertThatThrownBy(() -> new Product("  ", DEFAULT_PRICE, DEFAULT_STOCK))
                        .isInstanceOfSatisfying(InvalidProductNameException.class, e -> {
                            assertThat(e.getReason()).isEqualTo(InvalidProductNameException.Reason.BLANK);
                            assertThat(e.getMessage())
                                    .isEqualTo(DomainExceptionMessage.PARAM_REQUIRE_NOT_BLANK.text(paramMap.get("name")));
                        });
            }
        }

        @Nested
        class Price {
            @Test
            @DisplayName("null -> 예외")
            void shouldFail_whenPriceIsNull() {
                assertThatThrownBy(() -> new Product(DEFAULT_NAME, null, DEFAULT_STOCK))
                        .isInstanceOfSatisfying(InvalidProductPriceException.class, e -> {
                            assertThat(e.getReason()).isEqualTo(InvalidProductPriceException.Reason.REQUIRED);
                            assertThat(e.getMessage()).isEqualTo(DomainExceptionMessage.PARAM_REQUIRE_NOT_NULL.text(paramMap.get("price")));
                        });
            }

            @Test
            @DisplayName("음수 -> 예외")
            void shouldFail_whenPriceIsNegative() {
                assertThatThrownBy(() -> new Product(DEFAULT_NAME, DEFAULT_NEGATIVE, DEFAULT_STOCK))
                        .isInstanceOfSatisfying(InvalidProductPriceException.class, e -> {
                            assertThat(e.getReason()).isEqualTo(InvalidProductPriceException.Reason.NEGATIVE);
                            assertThat(e.getMessage())
                                    .isEqualTo(DomainExceptionMessage.PARAM_CANNOT_BE_NEGATIVE.text(paramMap.get("price"), DEFAULT_NEGATIVE));
                        });
            }
        }

        @Nested
        class Stock {
            @Test
            @DisplayName("null -> 예외")
            void shouldFail_whenStockIsNull() {
                assertThatThrownBy(() -> new Product(DEFAULT_NAME, DEFAULT_PRICE, null))
                        .isInstanceOfSatisfying(InvalidProductStockException.class, e -> {
                            assertThat(e.getReason()).isEqualTo(InvalidProductStockException.Reason.REQUIRED);
                            assertThat(e.getMessage())
                                    .isEqualTo(DomainExceptionMessage.PARAM_REQUIRE_NOT_NULL.text(paramMap.get("stock")));
                        });
            }

            @Test
            @DisplayName("음수 -> 예외")
            void shouldFail_whenStockIsNegative() {
                assertThatThrownBy(() -> new Product(DEFAULT_NAME, DEFAULT_PRICE, DEFAULT_NEGATIVE))
                        .isInstanceOfSatisfying(InvalidProductStockException.class, e -> {
                            assertThat(e.getReason())
                                    .isEqualTo(InvalidProductStockException.Reason.NEGATIVE);
                            assertThat(e.getMessage())
                                    .isEqualTo(DomainExceptionMessage.PARAM_CANNOT_BE_NEGATIVE.text(paramMap.get("stock"), DEFAULT_NEGATIVE));
                        });
            }
        }
    }

    @Nested
    class Price {
        @Nested
        @DisplayName("changePrice(int)")
        class ChangePrice{
            @Test
            @DisplayName("정상 -> 변경")
            void success() {
                //given
                Product product = new Product(DEFAULT_NAME, 10_000, DEFAULT_STOCK);

                //when
                product.changePrice(50_000);

                //then
                assertThat(product.getPrice()).isEqualTo(50_000);
            }

            @Test
            @DisplayName("음수 변경 -> 예외")
            void shouldFail_whenChangeNegativePrice() {
                //given
                Product product = new Product(DEFAULT_NAME, 10_000, DEFAULT_STOCK);

                //when, then
                assertThatThrownBy(() -> product.changePrice(DEFAULT_NEGATIVE))
                        .isInstanceOfSatisfying(InvalidProductPriceException.class, e -> {
                            assertThat(e.getReason()).isEqualTo(InvalidProductPriceException.Reason.NEGATIVE);
                            assertThat(e.getMessage())
                                    .isEqualTo(DomainExceptionMessage.PARAM_CANNOT_BE_NEGATIVE.text(paramMap.get("price"), DEFAULT_NEGATIVE));
                        });
            }
        }

    }



    @Nested
    class Stock {
        @Nested
        @DisplayName("addStock(int)")
        class Add {
            @Test
            @DisplayName("정상 -> 누적 증가")
            void success() {
                //given
                Product product = new Product(DEFAULT_NAME, DEFAULT_PRICE, 50);

                //when
                product.addStock(50);

                //then
                assertThat(product.getStockQuantity()).isEqualTo(100);
            }
        }

        @Nested
        @DisplayName("reduceStock(int)")
        class Reduce{
            @Test
            @DisplayName("정상 -> 차감")
            void success() {
                //given
                Product product = new Product(DEFAULT_NAME, DEFAULT_PRICE, 50);

                //when
                product.reduceStock(30);

                //then
                assertThat(product.getStockQuantity()).isEqualTo(20);

            }

            @Test
            @DisplayName("차감 수량 > 재고 수량 -> 예외")
            void shouldFail_whenInsufficient() {
                //given
                Product product = new Product(DEFAULT_NAME, DEFAULT_PRICE, 50);

                //when, then
                assertThatThrownBy(() -> product.reduceStock(9999))
                        .isInstanceOfSatisfying(InvalidProductStockException.class, e -> {
                            assertThat(e.getReason()).isEqualTo(InvalidProductStockException.Reason.INSUFFICIENT);
                            assertThat(e.getMessage()).isEqualTo(ProductMessage.STOCK_INSUFFICIENT.text(9999, 50));
                        });

            }
        }

        @Nested
        @DisplayName("removeAllStock()")
        class RemoveAllStock{
            @Test
            @DisplayName("정상 -> 제거")
            void success() {
                //given
                Product product = new Product(DEFAULT_NAME, DEFAULT_PRICE, 50);

                //when
                product.removeAllStock();

                //then
                assertThat(product.getStockQuantity()).isZero();
            }
        }
    }

    @Nested
    class Name {
        @Nested
        @DisplayName("changeName(String)")
        class Change{
            @Test
            @DisplayName("정상 -> 변경")
            void success() {
                //given
                Product product = new Product(DEFAULT_NAME, DEFAULT_PRICE, DEFAULT_STOCK);

                //when
                product.changeName("바뀐 이름");

                //then
                assertThat(product.getName()).isEqualTo("바뀐 이름");
            }

            @Test
            @DisplayName("null -> 예외")
            void shouldFail_whenNameIsNull() {
                //given
                Product product = new Product(DEFAULT_NAME, DEFAULT_PRICE, DEFAULT_STOCK);

                //then
                assertThatThrownBy(() -> product.changeName(null))
                        .isInstanceOfSatisfying(InvalidProductNameException.class, e -> {
                            assertThat(e.getReason()).isEqualTo(InvalidProductNameException.Reason.REQUIRED);
                        });
            }

            @Test
            @DisplayName("blank -> 예외")
            void shouldFail_whenNameIsBlank() {
                //given
                Product product = new Product(DEFAULT_NAME, DEFAULT_PRICE, DEFAULT_STOCK);

                //then
                assertThatThrownBy(() -> product.changeName("      "))
                        .isInstanceOfSatisfying(InvalidProductNameException.class, e -> {
                            assertThat(e.getReason()).isEqualTo(InvalidProductNameException.Reason.BLANK);
                        });
            }
        }
    }
}
