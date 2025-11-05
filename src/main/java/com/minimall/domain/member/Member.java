package com.minimall.domain.member;

import com.minimall.domain.common.base.BaseEntity;
import com.minimall.domain.embeddable.InvalidAddressException;
import com.minimall.domain.exception.Guards;
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

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "member_id")
    private Long id;

    @Column(nullable = false)
    private String loginId;

    @Column(nullable = false)
    private String password;

    @Column(name = "member_name", nullable = false)
    private String name;

    @Column(nullable = false)
    private String email;

    @Embedded
    private Address addr;

    @Column(nullable = false)
    private Grade grade; //TODO 할인등급 적용(DB 추가)

    @OneToMany(mappedBy = "member")
    private List<Order> orders = new ArrayList<>();

    //== 생성자 ==//
    private Member(String loginId, String password, String name, String email, Address addr, Grade grade) {
        this.loginId = loginId;
        this.password = password;
        this.name = name;
        this.email = email;
        this.addr = addr;
        this.grade = grade;
    }

    @Builder
    public static Member create(String loginId, String password, String name, String email, Address addr) {
        validateCreate(loginId, password, name, email, addr);

        Grade defaultGrade = Grade.BRONZE;

        return new Member(loginId, password, name, email, addr, defaultGrade);
    }


    //== 연관관계 편의 메서드 ==//
    public void addOrder(Order order) {
        if (!orders.contains(order)) {
            orders.add(order);
        }
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

    public void changeGrade(Grade newGrade) {
        this.grade = newGrade;
    }

    //== 검증 메서드 ==//
    private static void validateCreate(String loginId, String password, String name, String email, Address addr) {
        Guards.requireNotNullAndNotBlank(loginId,
                InvalidLoginIdException::required,
                InvalidLoginIdException::blank);

        Guards.requireNotNullAndNotBlank(password,
                InvalidPasswordException::required,
                InvalidPasswordException::blank);

        Guards.requireNotNullAndNotBlank(name,
                InvalidMemberNameException::required,
                InvalidMemberNameException::blank);

        Guards.requireNotNullAndNotBlank(email,
                InvalidEmailException::required,
                InvalidEmailException::blank);

        Guards.requireNotNull(addr, InvalidAddressException::required);
    }
}
