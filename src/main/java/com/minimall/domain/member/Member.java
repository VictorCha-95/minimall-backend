package com.minimall.domain.member;

import com.minimall.common.base.BaseEntity;
import com.minimall.domain.order.Order;
import com.minimall.domain.embeddable.Address;
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


    //== 연관관계 편의 메서드 ==//
    public void addOrder(Order order) {
        orders.add(order);
        if (order.getMember() != this) {
            order.setMember(this);
        }
    }

    public void removeOrder(Order order) {
        orders.remove(order);
    }


    //== 필드 수정용 메서드 ==//
    public void update(String password, String name, String email, Address addr) {
        //TODO password, Address 형식 검증 추가
        if (password != null) this.password = password;
        if (name != null) this.name = name;
        if (email != null) this.email = email;
        if (addr != null) this.addr = addr;
    }

    public void changePassword(String newPassword) {
        this.password = newPassword;
    }

    public void upgradeGrade(Grade newGrade) {
        this.grade = newGrade;
    }
}
