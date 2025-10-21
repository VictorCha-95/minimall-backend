package com.minimall.api.domain.order;

import com.minimall.api.common.base.BaseEntity;
import com.minimall.api.domain.product.Product;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;

@Entity
@Getter
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
    @Builder
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
