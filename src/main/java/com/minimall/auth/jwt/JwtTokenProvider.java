package com.minimall.auth.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.time.Clock;
import java.time.Instant;
import java.util.Base64;
import java.util.Date;
import java.util.UUID;

@Component
public final class JwtTokenProvider {

    private final SecretKey key;
    private final String issuer;
    private final long accessTtlSeconds;
    private final Clock clock;

    public JwtTokenProvider(JwtProperties props, Clock clock) {
        this.issuer = props.issuer();
        this.accessTtlSeconds = props.accessTtlSeconds();
        this.clock = clock;

        byte[] decoded = Base64.getDecoder().decode(props.secretBase64());
        this.key = Keys.hmacShaKeyFor(decoded); // HS256 최소 32바이트 요구
    }

    public String createAccessToken(long memberId, String role) {
        Instant now = clock.instant();
        Instant exp = now.plusSeconds(accessTtlSeconds);

        return Jwts.builder()
                .issuer(issuer)
                .subject(String.valueOf(memberId))
                .id(UUID.randomUUID().toString())  // jti
                .issuedAt(Date.from(now))
                .expiration(Date.from(exp))
                .claim("role", role)
                .signWith(key) // HS256
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
