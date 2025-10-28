package com.minimall.domain.order;

import com.minimall.domain.common.base.BaseEntity;
import com.minimall.domain.product.Product;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
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
        product.removeStock(orderQuantity);
        return OrderItem.builder()
                .product(product)
                .productName(product.getName())
                .orderPrice(product.getPrice())
                .orderQuantity(orderQuantity)
                .build();
    }


    @Builder(access = AccessLevel.PRIVATE)
    public OrderItem(Order order, Product product, String productName, Integer orderPrice, Integer orderQuantity) {
        this.order = order;
        this.product = product;
        this.productName = productName;
        this.orderPrice = orderPrice;
        this.orderQuantity = orderQuantity;
    }

    //==연관관계 메서드==//
    public void setOrder(Order order) {
        this.order = order;
        if (!order.getOrderItems().contains(this)) {
            order.addOrderItem(this);
        }
    }

}
