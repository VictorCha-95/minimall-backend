package com.minimall.api.domain.member;

import com.minimall.api.common.base.BaseEntity;
import com.minimall.api.domain.order.Order;
import com.minimall.api.embeddable.Address;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Member extends BaseEntity {

    @Id @GeneratedValue
    @Column(name = "member_id")
    private Long id;

    private String loginId;
    private String password;

    @Column(name = "member_name")
    private String name;

    private String email;

    private Grade grade; //TODO 할인등급 적용(DB 추가)

    @Embedded
    private Address addr;

    @OneToMany(mappedBy = "member")
    private List<Order> orders = new ArrayList<>();

    @Builder
    public Member(String loginId, String password, String name, String email, Address addr) {
        this.loginId = loginId;
        this.password = password;
        this.name = name;
        this.email = email;
        this.addr = addr;
        grade = Grade.BRONZE;
    }

    //==연관관계 편의 메서드==//
    public void addOrder(Order order) {
        orders.add(order);
        if (order.getMember() != this) {
            order.setMember(this);
        }
    }

    public void removeOrder(Order order) {
        orders.remove(order);
    }
}
