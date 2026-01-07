package com.minimall.auth.jwt;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "jwt")
public record JwtProperties(
        @NotBlank String issuer,
        @Positive long accessTtlSeconds,
        @Positive long refreshTtlSeconds,
        @NotBlank String secretBase64
) {}
