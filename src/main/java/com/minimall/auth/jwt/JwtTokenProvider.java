package com.minimall.auth.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.security.Keys;
import lombok.Getter;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import javax.crypto.SecretKey;
import java.time.Clock;
import java.time.Instant;
import java.util.Base64;
import java.util.Date;
import java.util.UUID;

@Component
@Getter
public final class JwtTokenProvider {

    private static final String TOKEN_TYPE = "typ";
    private static final String TOKEN_TYPE_REFRESH = "refresh";

    private final SecretKey key;
    private final String issuer;
    private final long accessTtlSeconds;
    private final long refreshTtlSeconds;
    private final Clock clock;

    public JwtTokenProvider(@Validated JwtProperties props, Clock clock) {
        this.issuer = props.issuer();
        this.accessTtlSeconds = props.accessTtlSeconds();
        this.refreshTtlSeconds = props.refreshTtlSeconds();
        this.clock = clock;

        byte[] decoded = Base64.getDecoder().decode(props.secretBase64());
        if (decoded.length < 32) {
            throw new IllegalArgumentException("jwt.secretBase64 must decode to at least 32 bytes for HS256");
        }
        this.key = Keys.hmacShaKeyFor(decoded);

    }

    public String createAccessToken(long memberId, String role) {
        String authority = (role != null && role.startsWith("ROLE_")) ? role : "ROLE_" + role;

        Instant now = clock.instant();
        Instant exp = now.plusSeconds(accessTtlSeconds);

        return Jwts.builder()
                .issuer(issuer)
                .subject(String.valueOf(memberId))
                .id(UUID.randomUUID().toString())
                .issuedAt(Date.from(now))
                .expiration(Date.from(exp))
                .claim("role", authority)
                .signWith(key)
                .compact();
    }

    public String createRefreshToken(long memberId, String refreshJti) {
        Instant now = clock.instant();
        Instant exp = now.plusSeconds(refreshTtlSeconds);

        return Jwts.builder()
                .issuer(issuer)
                .subject(String.valueOf(memberId))
                .id(refreshJti)
                .issuedAt(Date.from(now))
                .expiration(Date.from(exp))
                .claim(TOKEN_TYPE, TOKEN_TYPE_REFRESH)
                .signWith(key)
                .compact();
    }


    public Claims parseAndValidate(String token) throws JwtException {
        io.jsonwebtoken.Clock jjwtClock = () -> Date.from(clock.instant());

        return Jwts.parser()
                .clock(jjwtClock)
                .verifyWith(key)
                .requireIssuer(issuer)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
