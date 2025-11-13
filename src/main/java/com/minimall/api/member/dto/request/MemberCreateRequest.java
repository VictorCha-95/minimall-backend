package com.minimall.api.member.dto.request;

import com.minimall.domain.embeddable.Address;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.springframework.lang.Nullable;

public record MemberCreateRequest(
        @NotBlank String loginId,
        @NotBlank String password, //TODO 패턴 애너테이션 추가
        @NotBlank String name, //TODO 사이즈 애너테이션 추가
        @NotBlank @Email String email,
        @Nullable Address addr
) {
}
