package com.minimall.domain.product;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.*;

import static org.assertj.core.api.Assertions.*;


@DisplayName("Product 도메인")
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
public class ProductTest {

    @Nested
    class Create {
        @Test
        @DisplayName("이름/가격/재고 설정 -> 성공")
        void success() {
            Product product = new Product("마우스", 10_000, 50);
        }

        @Test
        @DisplayName("이름 null 또는 빈 문자열 -> 예외 발생")
        void shouldFail_whenNameIsNullOrEmpty() {
            assertThatThrownBy(() -> new Product(null, ))
        }
    }

}
