package com.minimall.service.auth;

import com.minimall.auth.jwt.JwtTokenProvider;
import com.minimall.domain.member.Member;
import com.minimall.domain.member.MemberRepository;
import com.minimall.service.auth.dto.LoginCommand;
import com.minimall.service.auth.dto.LoginResult;
import com.minimall.service.exception.InvalidCredentialException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    public LoginResult login(LoginCommand command) {
        Member member = memberRepository.findByLoginId(command.loginId())
            .orElseThrow(() -> InvalidCredentialException.invalidLoginId(command.loginId()));

        if (!passwordEncoder.matches(command.password(), member.getPasswordHash())) {
            throw InvalidCredentialException.invalidPassword(command.password());
        }

        String authority = "ROLE_" + member.getRole().name();

        String token = jwtTokenProvider.createAccessToken(member.getId(), authority);

        return new LoginResult("Bearer", token, jwtTokenProvider.getAccessTtlSeconds());
    }
}