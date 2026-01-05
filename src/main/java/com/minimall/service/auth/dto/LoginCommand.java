package com.minimall.service.auth.dto;

public record LoginCommand(
        String loginId,
        String password
) {}