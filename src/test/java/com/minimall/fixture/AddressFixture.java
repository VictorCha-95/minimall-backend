package com.minimall.fixture;

import com.minimall.api.common.embeddable.AddressDto;
import com.minimall.api.member.dto.request.MemberAddressRequest;
import com.minimall.domain.embeddable.Address;
import com.minimall.service.member.dto.command.MemberAddressCommand;

public final class AddressFixture {

    public static final String DEFAULT_POSTCODE = "12345";
    public static final String DEFAULT_STATE = "서울특별시";
    public static final String DEFAULT_CITY = "강남구";
    public static final String DEFAULT_STREET = "테헤란로 1";
    public static final String DEFAULT_DETAIL = "101동 202호";

    private AddressFixture() {
    }

    public static Address createAddress() {
        return createAddress(DEFAULT_POSTCODE, DEFAULT_STATE, DEFAULT_CITY, DEFAULT_STREET, DEFAULT_DETAIL);
    }

    public static Address createAddress(
            String postcode,
            String state,
            String city,
            String street,
            String detail
    ) {
        return Address.createAddress(postcode, state, city, street, detail);
    }

    public static AddressDto createAddressDto() {
        return createAddressDto(DEFAULT_POSTCODE, DEFAULT_STATE, DEFAULT_CITY, DEFAULT_STREET, DEFAULT_DETAIL);
    }

    public static AddressDto createAddressDto(
            String postcode,
            String state,
            String city,
            String street,
            String detail
    ) {
        return new AddressDto(postcode, state, city, street, detail);
    }

    public static MemberAddressCommand createMemberAddressCommand() {
        return createMemberAddressCommand(DEFAULT_POSTCODE, DEFAULT_STATE, DEFAULT_CITY, DEFAULT_STREET, DEFAULT_DETAIL);
    }

    public static MemberAddressCommand createMemberAddressCommand(
            String postcode,
            String state,
            String city,
            String street,
            String detail
    ) {
        return new MemberAddressCommand(postcode, state, city, street, detail);
    }

    public static MemberAddressRequest createMemberAddressRequest() {
        return createMemberAddressRequest(DEFAULT_POSTCODE, DEFAULT_STATE, DEFAULT_CITY, DEFAULT_STREET, DEFAULT_DETAIL);
    }

    public static MemberAddressRequest createMemberAddressRequest(
            String postcode,
            String state,
            String city,
            String street,
            String detail
    ) {
        return new MemberAddressRequest(postcode, state, city, street, detail);
    }
}
