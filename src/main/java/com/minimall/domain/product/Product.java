package com.minimall.domain.product;

import com.minimall.domain.common.base.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.util.StringUtils;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Product extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "product_id")
    private Long id;

    @Column(name = "product_name")
    private String name;

    @Column(name = "product_price")
    private Integer price;

    private Integer stockQuantity;


    //== 생성자 ==//
    public Product(String name, Integer price, Integer stockQuantity) {
        validateFields(name, price, stockQuantity);
        this.name = name;
        this.price = price;
        this.stockQuantity = stockQuantity;
    }


    //== 비즈니스 로직 ==//
    public void changePrice(int price) {
        if (price < 0) {
            throw InvalidPriceException.priceCannotBeNegative(price);
        }
        this.price = price;
    }

    public void increaseStock(int quantity) {
        stockQuantity += quantity;
    }

    public void reduceStock(int quantity) {
        int realQuantity = stockQuantity - quantity;
        if (realQuantity < 0) {
            throw InsufficientStockException.ofStockQuantity(stockQuantity);
        }
        stockQuantity = realQuantity;
    }

    public void removeAllStock() {
        stockQuantity = 0;
    }


    //== 검증 로직 ==//
    private void validateFields(String name, Integer price, Integer stockQuantity) {
        if (!StringUtils.hasText(name)) {
            throw InvalidProductNameException.empty();
        }

        if (price == null) {
            throw InvalidPriceException.priceCannotBeNull();
        }

        if (price < 0) {
            throw InvalidPriceException.priceCannotBeNegative(price);
        }

        if (stockQuantity == null) {
            throw InvalidProductStockException.cannotBeNull();
        }

        if (stockQuantity < 0) {
            throw InvalidProductStockException.cannotBeNegative(stockQuantity);
        }
    }
}
