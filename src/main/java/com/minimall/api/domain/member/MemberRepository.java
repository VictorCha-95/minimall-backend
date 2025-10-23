package com.minimall.api.domain.member;

import org.springframework.data.jpa.repository.JpaRepository;

import javax.swing.text.html.Option;
import java.util.List;
import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {

    List<Member> findByName(String name);

    Optional<Member> findByEmail(String email);

    Optional<Member> findByLoginId(String loginId);

    Boolean existsByEmail(String email);

    Boolean existsByLoginId(String loginId);

}
