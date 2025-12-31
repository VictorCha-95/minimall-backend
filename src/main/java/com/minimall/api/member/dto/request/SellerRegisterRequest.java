package com.minimall.api.member.dto.request;

import com.minimall.service.member.dto.command.MemberAddressCommand;

public record SellerRegisterRequest(
        String loginId,
        String password,
        String name,
        String email,
        MemberAddressRequest addr,
        String storeName,
        String businessNumber,
        String account
) {}

