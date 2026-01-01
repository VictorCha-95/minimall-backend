package com.minimall.api;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class MeController {

    @GetMapping("/api/me")
    public Map<String, Object> me(Authentication authentication) {
        // JwtAuthenticationFilter에서 principal을 memberId(long)로 넣었음
        Object principal = authentication.getPrincipal();

        return Map.of(
                "principal", principal,
                "authorities", authentication.getAuthorities()
        );
    }
}
