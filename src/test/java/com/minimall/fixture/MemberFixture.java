package com.minimall.fixture;

import com.minimall.api.member.dto.request.MemberRegisterRequest;
import com.minimall.api.member.dto.request.MemberUpdateRequest;
import com.minimall.domain.embeddable.Address;
import com.minimall.domain.member.Member;
import com.minimall.domain.member.MemberRepository;
import com.minimall.service.member.dto.command.MemberRegisterCommand;

public final class MemberFixture {

    public static final String DEFAULT_LOGIN_ID = "member123";
    public static final String DEFAULT_PASSWORD = "12345";
    public static final String DEFAULT_NAME = "테스트회원";
    public static final String DEFAULT_EMAIL = "member123@example.com";

    private MemberFixture() {
    }

    public static Member createMember() {
        return createMember(DEFAULT_LOGIN_ID, DEFAULT_NAME, DEFAULT_EMAIL, AddressFixture.createAddress());
    }

    public static Member createMember(String loginId, String name, String email, Address address) {
        return Member.registerCustomer(loginId, DEFAULT_PASSWORD, name, email, address);
    }

    public static Member createMemberSaved(MemberRepository repository) {
        return repository.save(createMember());
    }

    public static Member createMemberSaved(MemberRepository repository, String loginId, String name) {
        return repository.save(createMember(loginId, name, loginId + "@example.com", AddressFixture.createAddress()));
    }

    public static Member createMemberSaved(
            MemberRepository repository,
            String loginId,
            String name,
            String email,
            Address address
    ) {
        return repository.save(createMember(loginId, name, email, address));
    }

    public static MemberRegisterCommand createRegisterCommand(String loginId, String name) {
        return createRegisterCommand(loginId, name, loginId + "@example.com", AddressFixture.createMemberAddressCommand());
    }

    public static MemberRegisterCommand createRegisterCommand(
            String loginId,
            String name,
            String email,
            com.minimall.service.member.dto.command.MemberAddressCommand address
    ) {
        return new MemberRegisterCommand(loginId, DEFAULT_PASSWORD, name, email, address);
    }

    public static MemberRegisterRequest createRegisterRequest(String loginId, String name) {
        return createRegisterRequest(loginId, name, loginId + "@example.com", AddressFixture.createMemberAddressRequest());
    }

    public static MemberRegisterRequest createRegisterRequest(
            String loginId,
            String name,
            String email,
            com.minimall.api.member.dto.request.MemberAddressRequest address
    ) {
        return new MemberRegisterRequest(loginId, DEFAULT_PASSWORD, name, email, address);
    }

    public static MemberUpdateRequest createUpdateRequest(
            String password,
            String name,
            String email,
            Address address
    ) {
        return new MemberUpdateRequest(password, name, email, address);
    }
}
