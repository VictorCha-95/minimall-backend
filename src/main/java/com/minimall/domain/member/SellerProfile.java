package com.minimall.domain.member;

import com.minimall.domain.common.base.BaseTimeEntity;
import com.minimall.domain.exception.Guards;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "member_seller_profile")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SellerProfile extends BaseTimeEntity {

    @Id
    @Column(name = "member_id")
    private Long memberId;

    @MapsId
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "member_id")
    private Member member;

    @Column(nullable = false, length = 100)
    private String storeName;

    @Column(nullable = false, length = 30, unique = true)
    private String businessNumber;

    @Column(nullable = false, length = 100)
    private String settlementAccount;

    private SellerProfile(Member member, String storeName, String businessNumber, String settlementAccount) {
        validateCreate(member, storeName, businessNumber, settlementAccount);

        this.member = member;
        this.storeName = storeName;
        this.businessNumber = businessNumber;
        this.settlementAccount = settlementAccount;
    }

    public static SellerProfile create(Member member, String storeName, String businessNumber, String settlementAccount) {
        return new SellerProfile(member, storeName, businessNumber, settlementAccount);
    }


    //== 필드 변경 메서드 ==//
    public void changeStoreName(String storeName) {
        Guards.requireNotBlank(storeName, () -> new IllegalArgumentException("storeName must not be blank"));
        this.storeName = storeName;
    }


    //== 검증 메서드 ==//
    public void validateCreate(Member member, String storeName, String businessNumber, String settlementAccount) {
        Guards.requireNotNull(member, () -> new IllegalArgumentException("member must not be null"));
        Guards.requireNotBlank(storeName, () -> new IllegalArgumentException("storeName must not be blank"));
        Guards.requireNotBlank(businessNumber, () -> new IllegalArgumentException("businessNumber must not be blank"));
        Guards.requireNotBlank(settlementAccount, () -> new IllegalArgumentException("settlementAccount must not be blank"));
    }
}
