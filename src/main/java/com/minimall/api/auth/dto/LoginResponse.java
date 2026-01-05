package com.minimall.api.auth.dto;

public record LoginResponse(
    String tokenType,
    String accessToken,
    long expiresIn
) {}