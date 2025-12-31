package com.minimall.service.member.dto.command;

public record SellerRegisterCommand(
        String loginId,
        String password,
        String name,
        String email,
        MemberAddressCommand addr,
        String storeName,
        String businessNumber,
        String account
) {}

