package com.minimall.service.auth;

import com.minimall.auth.jwt.JwtTokenProvider;
import com.minimall.auth.jwt.RefreshTokenStore;
import com.minimall.domain.member.Member;
import com.minimall.domain.member.MemberRepository;
import com.minimall.service.auth.dto.LoginCommand;
import com.minimall.service.auth.dto.LoginResult;
import com.minimall.service.exception.InvalidCredentialException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthService {

    private static final String CLAIM_TOKEN_TYPE = "typ";
    private static final String REFRESH_TOKEN_TYPE = "refresh";

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenStore refreshTokenStore;

    @Transactional
    public LoginResult login(LoginCommand command) {
        Member member = memberRepository.findByLoginId(command.loginId())
            .orElseThrow(() -> InvalidCredentialException.invalidLoginId(command.loginId()));

        if (!passwordEncoder.matches(command.password(), member.getPasswordHash())) {
            throw InvalidCredentialException.invalidPassword(command.password());
        }

        String authority = "ROLE_" + member.getRole().name();

        String accessToken = jwtTokenProvider.createAccessToken(member.getId(), authority);

        String refreshJti = UUID.randomUUID().toString();
        String refreshToken = jwtTokenProvider.createRefreshToken(member.getId(), refreshJti);
        refreshTokenStore.save(
                member.getId(),
                refreshJti,
                Duration.ofSeconds(jwtTokenProvider.getRefreshTtlSeconds())
        );

        return new LoginResult(
                "Bearer",
                accessToken,
                jwtTokenProvider.getAccessTtlSeconds(),
                refreshToken,
                jwtTokenProvider.getRefreshTtlSeconds()
        );
    }

    @Transactional
    public LoginResult refresh(String refreshToken) {
        Claims claims = parseRefreshToken(refreshToken);
        validateRefreshToken(claims);

        long memberId = Long.parseLong(claims.getSubject());
        Member member = memberRepository.findById(memberId)
                .orElseThrow(InvalidCredentialException::invalidRefreshToken);

        String authority = "ROLE_" + member.getRole().name();
        String accessToken = jwtTokenProvider.createAccessToken(memberId, authority);

        String newRefreshJti = UUID.randomUUID().toString();
        String newRefreshToken = jwtTokenProvider.createRefreshToken(memberId, newRefreshJti);
        refreshTokenStore.save(
                memberId,
                newRefreshJti,
                Duration.ofSeconds(jwtTokenProvider.getRefreshTtlSeconds())
        );

        return new LoginResult(
                "Bearer",
                accessToken,
                jwtTokenProvider.getAccessTtlSeconds(),
                newRefreshToken,
                jwtTokenProvider.getRefreshTtlSeconds()
        );
    }

    @Transactional
    public void logout(String refreshToken) {
        Claims claims = parseRefreshToken(refreshToken);
        validateRefreshToken(claims);
        long memberId = Long.parseLong(claims.getSubject());
        refreshTokenStore.delete(memberId);
    }

    private void validateRefreshToken(Claims claims) {
        String tokenType = claims.get(CLAIM_TOKEN_TYPE, String.class);
        if (!REFRESH_TOKEN_TYPE.equals(tokenType)) {
            throw InvalidCredentialException.invalidRefreshToken();
        }

        long memberId = Long.parseLong(claims.getSubject());
        String storedJti = refreshTokenStore.findJti(memberId);
        if (storedJti == null || !storedJti.equals(claims.getId())) {
            refreshTokenStore.delete(memberId);
            throw InvalidCredentialException.invalidRefreshToken();
        }
    }

    private Claims parseRefreshToken(String refreshToken) {
        try {
            return jwtTokenProvider.parseAndValidate(refreshToken);
        } catch (JwtException | IllegalArgumentException e) {
            throw InvalidCredentialException.invalidRefreshToken();
        }
    }
}
