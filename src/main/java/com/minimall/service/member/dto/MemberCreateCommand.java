package com.minimall.service.member.dto;

public record MemberCreateCommand(
        String loginId,
        String password,
        String name,
        String email,
        MemberAddressCommand addr
) {

    public MemberCreateCommand withEncodedPassword(String encodedPassword){
        return new MemberCreateCommand(
                this.loginId,
                encodedPassword,
                this.name,
                this.email,
                this.addr
        );
    }
}
