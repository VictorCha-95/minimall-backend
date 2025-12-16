package com.minimall.controller.api.member.dto.request;

import jakarta.validation.constraints.NotBlank;
import org.springframework.lang.Nullable;

public record MemberAddressRequest(
        @NotBlank String postcode,
        @NotBlank String state,
        @NotBlank String city,
        @NotBlank String street,
        @Nullable String detail
) {
}
