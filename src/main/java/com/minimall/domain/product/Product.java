package com.minimall.domain.product;

import com.minimall.domain.common.base.BaseEntity;
import com.minimall.domain.exception.Guards;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Product extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "product_id")
    private Long id;

    @Column(name = "product_name", nullable = false)
    private String name;

    @Column(name = "product_price", nullable = false)
    private Integer price;

    @Column(nullable = false)
    private Integer stockQuantity;


    //== 생성자 ==//
    public Product(String name, Integer price, Integer stockQuantity) {
        validateCreateParam(name, price, stockQuantity);
        this.name = name;
        this.price = price;
        this.stockQuantity = stockQuantity;
    }


    //== 비즈니스 로직 ==//
    public void changeName(String name) {
        Guards.requireNotNullAndNotBlank(name,
                InvalidProductNameException::required,
                InvalidProductNameException::blank);
        this.name = name;
    }

    public void changePrice(int price) {
        Guards.requireNonNegative(price, () -> InvalidProductPriceException.negative(price));
        this.price = price;
    }

    public void addStock(int requestedQuantity) {
        Guards.requirePositive(requestedQuantity,
                () -> InvalidProductStockException.requirePositive(requestedQuantity));
        stockQuantity += requestedQuantity;
    }

    public void reduceStock(int requestedQuantity) {
        Guards.requirePositive(requestedQuantity,
                () -> InvalidProductStockException.requirePositive(requestedQuantity));

        int realQuantity = stockQuantity - requestedQuantity;
        Guards.requireNonNegative(realQuantity,
                () -> InvalidProductStockException.insufficient(requestedQuantity, stockQuantity));
        stockQuantity = realQuantity;
    }

    public void removeAllStock() {
        stockQuantity = 0;
    }


    //== 검증 로직 ==//
    private void validateCreateParam(String name, Integer price, Integer stockQuantity) {
        Guards.requireNotNullAndNotBlank(name,
                InvalidProductNameException::required,
                InvalidProductNameException::blank);

        Guards.requireNotNullAndNonNegative(price,
                InvalidProductPriceException::required,
                () -> InvalidProductPriceException.negative(price));

        Guards.requireNotNullAndNonNegative(stockQuantity,
                InvalidProductStockException::required,
                () -> InvalidProductStockException.negative(stockQuantity));
    }
}