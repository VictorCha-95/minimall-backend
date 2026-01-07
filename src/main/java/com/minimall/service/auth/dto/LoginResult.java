package com.minimall.service.auth.dto;

public record LoginResult(
    String tokenType,
    String accessToken,
    long accessExpiresIn,
    String refreshToken,
    long refreshExpiresIn
) {}
