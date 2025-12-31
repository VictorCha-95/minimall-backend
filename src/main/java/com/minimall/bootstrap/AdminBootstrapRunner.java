package com.minimall.bootstrap;

import com.minimall.domain.member.Member;
import com.minimall.domain.member.MemberRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class AdminBootstrapRunner implements ApplicationRunner {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.admin.enabled:false}")
    private boolean enabled;

    @Value("${app.admin.loginId:}")
    private String loginId;

    @Value("${app.admin.email:}")
    private String email;

    @Value("${app.admin.password:}")
    private String password;

    @Value("${app.admin.name:관리자}")
    private String name;

    public AdminBootstrapRunner(MemberRepository memberRepository, PasswordEncoder passwordEncoder) {
        this.memberRepository = memberRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (!enabled) return;
        if (isBlank(loginId) || isBlank(email) || isBlank(password)) return;

        if (memberRepository.existsByLoginId(loginId) || memberRepository.existsByEmail(email)) return;

        String hash = passwordEncoder.encode(password);
        Member admin = Member.registerAdmin(loginId, hash, name, email, null);
        memberRepository.save(admin);
    }

    private boolean isBlank(String s) { return s == null || s.trim().isEmpty(); }
}
