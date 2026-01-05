package com.minimall.api.auth;

import com.minimall.api.auth.dto.AuthApiMapper;
import com.minimall.api.auth.dto.LoginRequest;
import com.minimall.api.auth.dto.LoginResponse;
import com.minimall.service.auth.AuthService;
import com.minimall.service.auth.dto.LoginResult;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/auth", produces = "application/json")
@Tag(name = "Auth API", description = "인증 관련 API")
public class AuthController {

    private final AuthService authService;
    private final AuthApiMapper mapper;

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody @Valid LoginRequest request) {
        LoginResult result = authService.login(mapper.toLoginCommand(request));
        return ResponseEntity.ok(mapper.toLoginResponse(result));
    }


}
