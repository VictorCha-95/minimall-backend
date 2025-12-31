package com.minimall.service.member.dto.command;

public record MemberAddressCommand(
        String postcode,
        String state,
        String city,
        String street,
        String detail
) {
}
