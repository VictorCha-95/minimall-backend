package com.minimall.auth.jwt;

import java.time.Duration;

public interface RefreshTokenStore {
    void save(Long memberId, String refreshJti, Duration ttl);
    String findJti(Long memberId);
    void delete(Long memberId);
}