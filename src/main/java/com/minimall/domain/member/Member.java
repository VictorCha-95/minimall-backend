package com.minimall.domain.member;

import com.minimall.domain.common.base.BaseEntity;
import com.minimall.domain.exception.Guards;
import com.minimall.domain.member.exception.InvalidEmailException;
import com.minimall.domain.member.exception.InvalidLoginIdException;
import com.minimall.domain.member.exception.InvalidMemberNameException;
import com.minimall.domain.member.exception.InvalidPasswordException;
import com.minimall.domain.order.Order;
import com.minimall.domain.embeddable.Address;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Member extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "member_id")
    private Long id;

    @Column(nullable = false, unique = true)
    private String loginId;

    @Column(nullable = false)
    private String passwordHash;

    @Column(name = "member_name", nullable = false, length = 50)
    private String name;

    @Column(nullable = false, unique = true, length = 100)
    private String email;

    @Embedded
    private Address addr;

    @Column(name = "status", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private MemberStatus status;

    @Column(nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private Role role;

    @OneToOne(mappedBy = "member", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private CustomerProfile customerProfile;

    @OneToOne(mappedBy = "member", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private SellerProfile sellerProfile;

    @OneToMany(mappedBy = "member")
    private List<Order> orders = new ArrayList<>();

    //== 생성자 ==//
    private Member(String loginId, String passwordHash, String name, String email, Address addr, Role role) {
        validateCreate(loginId, passwordHash, name, email);
        this.loginId = loginId;
        this.passwordHash = passwordHash;
        this.name = name;
        this.email = email;
        this.addr = addr;
        this.role = Objects.requireNonNullElse(role, Role.CUSTOMER);
        this.status = MemberStatus.ACTIVE;
    }

    public static Member registerCustomer(String loginId, String passwordHash, String memberName, String email, Address address){
        Member member = new Member(loginId, passwordHash, memberName, email, address, Role.CUSTOMER);
        member.customerProfile = CustomerProfile.create(member);
        member.validateProfileConsistency();
        return member;
    }

    public static Member registerSeller(String loginId, String passwordHash, String memberName, String email, Address address,
                                        String storeName, String businessNumber, String settlementAccount) {
        Member member = new Member(loginId, passwordHash, memberName, email, address, Role.SELLER);
        SellerProfile profile = SellerProfile.create(member, storeName, businessNumber, settlementAccount);
        member.sellerProfile = profile;
        member.validateProfileConsistency();
        return member;
    }

    static Member registerAdmin(String loginId, String passwordHash, String memberName, String email, Address address){
        Member member = new Member(loginId, passwordHash, memberName, email, address, Role.ADMIN);
        return member;
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
        if (password != null) this.passwordHash = password;
        if (name != null) this.name = name;
        if (email != null) this.email = email;
        if (addr != null) this.addr = addr;
    }

    public void changePassword(String newPassword) {
        this.passwordHash = newPassword;
    }

    //== 검증 메서드 ==//
    private static void validateCreate(String loginId, String password, String name, String email) {
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
    }

    public void validateProfileConsistency() {
        if (role == Role.CUSTOMER) {
            if (customerProfile == null) throw new IllegalStateException("CUSTOMER requires CustomerProfile");
            if (sellerProfile != null) throw new IllegalStateException("CUSTOMER must not have SellerProfile");
        }
        if (role == Role.SELLER) {
            if (sellerProfile == null) throw new IllegalStateException("SELLER requires SellerProfile");
            if (customerProfile != null) throw new IllegalStateException("SELLER must not have CustomerProfile");
        }
    }
}