package com.minimall.domain.product;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DataJpaTest
public class ProductDomainTest {

    @Autowired
    ProductRepository productRepository;

    @Test
    @DisplayName("상품 저장/조회")
    void productBasicTest() {
        //given
        Product product1 = new Product("노트북", 1500000, 10);
        Product product2 = new Product("무선마우스", 35000, 50);
        Product product3 = new Product("기계식키보드", 120000, 30);

        //when
        productRepository.save(product1);
        productRepository.save(product2);
        productRepository.save(product3);
        List<Product> productList = productRepository.findAll();
        Product findProduct1 = productRepository.findById(product1.getId()).get();
        Product findProduct2 = productRepository.findById(product2.getId()).get();

        //then
        assertThat(productList.size()).isEqualTo(3);
        assertThat(findProduct1.getName()).isEqualTo("노트북");
        assertThat(findProduct1).isEqualTo(product1);
        assertThat(findProduct2).isEqualTo(product2);
    }

    @Test
    @DisplayName("남아 있는 재고보다 초과한 재고 감량은 오류 발생")
    void productStockTest() {
        //given
        Product product = new Product("노트북", 1500000, 10);
        productRepository.save(product);

        //when
        product.addStock(20);
        Product findProduct = productRepository.findById(product.getId()).get();

        //then
        assertThat(findProduct.getStockQuantity()).isEqualTo(30);
        assertThrows(IllegalArgumentException.class, () -> product.removeStock(10000)); // 재고 과다 감량 -> 오류 발생
    }
}
