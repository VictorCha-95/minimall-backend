package com.minimall.api.domain.product;

import com.minimall.api.common.base.BaseEntity;
import com.minimall.api.domain.order.Order;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import lombok.Getter;

@Entity
@Getter
public class Product extends BaseEntity {

    @Id @GeneratedValue
    @Column(name = "product_id")
    private Long id;

    @Column(name = "product_name")
    private String name;

    @Column(name = "product_price")
    private Integer price;

    private Integer stockQuantity;


    //==생성자==//
    public Product(String name, Integer price, Integer stockQuantity) {
        this.name = name;
        this.price = price;
        this.stockQuantity = stockQuantity;
    }

    //==비즈니스 로직==//
    public void addStock(int quantity) {
        stockQuantity += quantity;
    }

    public void removeStock(int quantity) {
        int realQuantity = stockQuantity - quantity;
        if (realQuantity < 0) {
            throw new IllegalArgumentException("재고가 부족하여 요청하신 재고만큼 차감할 수 없습니다. 현재 재고: " + stockQuantity +
                    ", 요청한 재고 감량: " + quantity);
        }
        stockQuantity = realQuantity;
    }
}
