package com.minimall.domain.member;

import com.minimall.domain.common.base.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Objects;

@Entity
@Table(name = "member_customer_profile")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CustomerProfile extends BaseTimeEntity {

    @Id
    @Column(name = "member_id")
    private Long memberId;

    @MapsId
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "member_id")
    private Member member;

    @Enumerated(EnumType.STRING)
    @Column(name = "grade", nullable = false, length = 20)
    private CustomerGrade grade;


    //== 생성자 ==//
    private CustomerProfile(Member member, CustomerGrade grade) {
        this.member = Objects.requireNonNull(member, "member must not be null");
        this.grade = Objects.requireNonNullElse(grade, CustomerGrade.BRONZE);
    }

    public static CustomerProfile create(Member member) {
        return new CustomerProfile(member, CustomerGrade.BRONZE);
    }

    public static CustomerProfile create(Member member, CustomerGrade grade) {
        return new CustomerProfile(member, grade);
    }

    //== 메서드 ==//
    public void changeGrade(CustomerGrade newGrade) {
        this.grade = Objects.requireNonNull(newGrade, "newGrade must not be null");
    }
}
