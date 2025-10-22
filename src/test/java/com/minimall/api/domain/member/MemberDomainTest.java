package com.minimall.api.domain.member;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
public class MemberDomainTest {

    @Autowired
    MemberRepository memberRepository;

    @Test
    @DisplayName("멤버 저장/조회")
    void memberBasicTest() {
        //given
        Member member = createMember();

        //when
        memberRepository.save(member);
        Member findMember = memberRepository.findById(member.getId()).get();

        //then
        assertThat(findMember.getName()).isEqualTo("차태승");
        assertThat(findMember.getGrade()).isEqualTo(Grade.BRONZE); // default Grade: BRONZE
    }


    //==Helper Methods==//
    private Member createMember() {
        return Member.builder()
                .loginId("user1")
                .password("abc12345")
                .name("차태승")
                .email("cts9458@naver.com")
                .build();
    }
}

