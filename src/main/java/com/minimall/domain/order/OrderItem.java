package com.minimall.domain.order;

import com.minimall.domain.common.base.BaseEntity;
import com.minimall.domain.order.exception.InvalidOrderQuantityException;
import com.minimall.domain.product.Product;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OrderItem extends BaseEntity {

    @Id
    @GeneratedValue
    @Column(name = "order_item_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    private Product product;

    private String productName;
    private Integer orderPrice;
    private Integer orderQuantity;


    //==생성자 메서드==//
    public static OrderItem createOrderItem(Product product, int orderQuantity) {
        if (orderQuantity <= 0) {
            throw InvalidOrderQuantityException.mustBeGreaterThanZero(orderQuantity);
        }

        product.reduceStock(orderQuantity);

        return new OrderItem(product, product.getName(), product.getPrice(), orderQuantity);
    }


    private OrderItem(Product product, String productName, Integer orderPrice, Integer orderQuantity) {
        this.product = product;
        this.productName = productName;
        this.orderPrice = orderPrice;
        this.orderQuantity = orderQuantity;
    }


    //== 연관관계 메서드 ==//
    void assignOrder(Order order) {
        this.order = order;
    }


    //== 비즈니스 로직 ==//
    public int createTotalAmount() {
        return orderPrice * orderQuantity;
    }

}
