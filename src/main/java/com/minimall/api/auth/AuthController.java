package com.minimall.api.auth;

import com.minimall.api.auth.dto.AuthApiMapper;
import com.minimall.api.auth.dto.AuthMeResponse;
import com.minimall.api.auth.dto.LoginRequest;
import com.minimall.api.auth.dto.LoginResponse;
import com.minimall.api.common.embeddable.AddressDto;
import com.minimall.api.common.embeddable.AddressMapper;
import com.minimall.service.auth.AuthService;
import com.minimall.service.auth.dto.LoginResult;
import com.minimall.service.exception.InvalidCredentialException;
import com.minimall.service.member.MemberService;
import com.minimall.service.member.dto.result.MemberMeResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/api/auth", produces = "application/json")
@Tag(name = "Auth API", description = "인증 관련 API")
public class AuthController {

    private static final String REFRESH_TOKEN_COOKIE = "refreshToken";

    private final AuthService authService;
    private final MemberService memberService;
    private final AuthApiMapper mapper;
    private final AddressMapper addressMapper;

    @Operation(summary = "로그인", description = "Access/Refresh 토큰을 발급합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "로그인 성공"),
            @ApiResponse(responseCode = "401", description = "인증 실패")
    })
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody @Valid LoginRequest request, HttpServletRequest httpRequest) {
        LoginResult result = authService.login(mapper.toLoginCommand(request));
        ResponseCookie cookie = buildRefreshCookie(result.refreshToken(), result.refreshExpiresIn(), httpRequest);
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(mapper.toLoginResponse(result));
    }

    @Operation(summary = "내 정보 조회", description = "Access 토큰 기반으로 내 정보 조회")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "401", description = "인증 실패")
    })
    @GetMapping("/me")
    public ResponseEntity<AuthMeResponse> me(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        Long memberId = extractMemberId(authentication);
        MemberMeResult result = memberService.getMe(memberId);
        AddressDto address = (result.addr() == null) ? null : addressMapper.toDto(result.addr());
        return ResponseEntity.ok(AuthMeResponse.from(result, address));
    }

    @Operation(summary = "토큰 재발급", description = "Refresh 토큰을 검증해 새 Access/Refresh를 발급합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "재발급 성공"),
            @ApiResponse(responseCode = "401", description = "리프레시 토큰 오류")
    })
    @PostMapping("/refresh")
    public ResponseEntity<LoginResponse> refresh(
            @CookieValue(name = REFRESH_TOKEN_COOKIE, required = false) String refreshToken,
            HttpServletRequest httpRequest
    ) {
        if (refreshToken == null || refreshToken.isBlank()) {
            throw InvalidCredentialException.invalidRefreshToken();
        }

        LoginResult result = authService.refresh(refreshToken);
        ResponseCookie cookie = buildRefreshCookie(result.refreshToken(), result.refreshExpiresIn(), httpRequest);
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(mapper.toLoginResponse(result));
    }

    @Operation(summary = "로그아웃", description = "Refresh 토큰을 폐기합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "로그아웃 성공"),
            @ApiResponse(responseCode = "401", description = "리프레시 토큰 오류")
    })
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
            @CookieValue(name = REFRESH_TOKEN_COOKIE, required = false) String refreshToken,
            HttpServletRequest httpRequest
    ) {
        if (refreshToken == null || refreshToken.isBlank()) {
            throw InvalidCredentialException.invalidRefreshToken();
        }

        authService.logout(refreshToken);
        ResponseCookie cleared = clearRefreshCookie(httpRequest);
        return ResponseEntity.noContent()
                .header(HttpHeaders.SET_COOKIE, cleared.toString())
                .build();
    }

    private ResponseCookie buildRefreshCookie(String refreshToken, long refreshTtlSeconds, HttpServletRequest request) {
        return ResponseCookie.from(REFRESH_TOKEN_COOKIE, refreshToken)
                .httpOnly(true)
                .secure(request.isSecure())
                .sameSite("Lax")
                .path("/api/auth")
                .maxAge(refreshTtlSeconds)
                .build();
    }

    private ResponseCookie clearRefreshCookie(HttpServletRequest request) {
        return ResponseCookie.from(REFRESH_TOKEN_COOKIE, "")
                .httpOnly(true)
                .secure(request.isSecure())
                .sameSite("Lax")
                .path("/api/auth")
                .maxAge(0)
                .build();
    }

    private Long extractMemberId(Authentication authentication) {
        Object principal = authentication.getPrincipal();
        if (principal instanceof Long memberId) {
            return memberId;
        }
        if (principal instanceof String value) {
            return Long.parseLong(value);
        }

        throw new IllegalStateException("Unsupported principal type: " + principal.getClass().getName());
    }

}
