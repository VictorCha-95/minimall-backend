package com.minimall.auth.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Base64;

import static org.assertj.core.api.Assertions.*;

class JwtTokenProviderTest {

    private static JwtProperties props(String issuer, long ttlSeconds, byte[] rawKey) {
        return new JwtProperties(
                issuer,
                ttlSeconds,
                Base64.getEncoder().encodeToString(rawKey)
        );
    }

    @Test
    void should_create_and_validate_access_token() {
        // given
        byte[] key = new byte[64];
        for (int i = 0; i < key.length; i++) key[i] = (byte) i;

        Clock clock = Clock.fixed(Instant.parse("2025-12-31T00:00:00Z"), ZoneOffset.UTC);
        JwtTokenProvider jwt = new JwtTokenProvider(props("minimall", 600, key), clock);

        // when
        String token = jwt.createAccessToken(1L, "ROLE_CUSTOMER");
        Claims claims = jwt.parseAndValidate(token);

        // then
        assertThat(claims.getSubject()).isEqualTo("1");
        assertThat(claims.get("role", String.class)).isEqualTo("ROLE_CUSTOMER");
        assertThat(claims.getId()).isNotBlank();
        assertThat(claims.getIssuer()).isEqualTo("minimall");
    }

    @Test
    void should_fail_when_token_is_expired() {
        byte[] key = new byte[64];
        for (int i = 0; i < key.length; i++) key[i] = (byte) i;

        Clock atIssue = Clock.fixed(Instant.parse("2025-12-31T00:00:00Z"), ZoneOffset.UTC);
        JwtTokenProvider jwt = new JwtTokenProvider(props("minimall", 1, key), atIssue);

        String token = jwt.createAccessToken(1L, "ROLE_CUSTOMER");

        // 2초 후
        Clock afterExp = Clock.fixed(Instant.parse("2025-12-31T00:00:02Z"), ZoneOffset.UTC);
        JwtTokenProvider jwtAfterExp = new JwtTokenProvider(props("minimall", 1, key), afterExp);

        assertThatThrownBy(() -> jwtAfterExp.parseAndValidate(token))
                .isInstanceOf(ExpiredJwtException.class);
    }

    @Test
    void should_fail_when_issuer_is_different() {
        byte[] key = new byte[64];
        for (int i = 0; i < key.length; i++) key[i] = (byte) i;

        Clock clock = Clock.fixed(Instant.parse("2025-12-31T00:00:00Z"), ZoneOffset.UTC);

        JwtTokenProvider jwtA = new JwtTokenProvider(props("minimall", 600, key), clock);
        String token = jwtA.createAccessToken(1L, "ROLE_CUSTOMER");

        JwtTokenProvider jwtB = new JwtTokenProvider(props("other-service", 600, key), clock);

        assertThatThrownBy(() -> jwtB.parseAndValidate(token))
                .isInstanceOf(JwtException.class);
    }
}
