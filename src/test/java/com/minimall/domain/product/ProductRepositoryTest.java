package com.minimall.domain.product;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

@DataJpaTest
class ProductRepositoryTest {

    @Autowired
    ProductRepository productRepository;

    @BeforeEach
    void before() {
        productRepository.save(new Product("노트북", 1500000, 10));
        productRepository.save(new Product("무선마우스", 35000, 50));
        productRepository.save(new Product("기계식키보드", 120000, 30));
    }

    @Test
    void findByName() {
        //when
        List<Product> found = productRepository.findByName("노트북");

        //then
        assertThat(found)
                .extracting(Product::getName)
                .containsExactly("노트북");
    }

    @Test
    void findByPriceLessThan() {
        //when
        List<Product> found = productRepository.findByPriceLessThan(500000);

        //then
        assertThat(found)
                .extracting(Product::getName)
                .containsExactlyInAnyOrder("무선마우스", "기계식키보드");
    }

    @Test
    void findByPriceGreaterThan() {
        //when
        List<Product> found = productRepository.findByPriceGreaterThan(100000);

        //then
        assertThat(found)
                .extracting(Product::getPrice)
                .containsExactlyInAnyOrder(1500000, 120000);
    }

    @Test
    void findByStockQuantityLessThan() {
        //when
        List<Product> found = productRepository.findByStockQuantityLessThan(30);

        //then
        assertThat(found)
                .extracting(Product::getStockQuantity)
                .containsExactlyInAnyOrder(10);
    }

    @Test
    void findByStockQuantityGreaterThan() {
        //when
        List<Product> found = productRepository.findByStockQuantityGreaterThan(20);

        //then
        assertThat(found)
                .extracting(Product::getStockQuantity)
                .containsExactlyInAnyOrder(30, 50);
    }
}