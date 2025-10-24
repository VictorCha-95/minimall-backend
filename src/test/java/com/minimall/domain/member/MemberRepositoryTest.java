package com.minimall.domain.member;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

@DataJpaTest
class MemberRepositoryTest {

    @Autowired
    MemberRepository memberRepository;

    @Test
    void findByName() {
        //given
        List<Member> members = createMembersWithDuplicateName();
        memberRepository.saveAll(members);

        //when
        List<Member> findMembers1 = memberRepository.findByName("차태승");
        List<Member> findMembers2 = memberRepository.findByName("손흥민");

        //then
        assertThat(findMembers1)
                .extracting(Member::getName)
                .containsExactly("차태승", "차태승");
        assertThat(findMembers2)
                .extracting(Member::getName)
                .containsExactly("손흥민");
    }

    @Test
    void findByEmail() {
        //given
        Member member = createMember();
        memberRepository.save(member);

        //when
        Optional<Member> found = memberRepository.findByEmail("user1@naver.com");
        Optional<Member> notFound = memberRepository.findByEmail("notFoundUser@example.com");

        //then
        assertThat(found).isPresent().get().isEqualTo(member);
        assertThat(notFound).isEmpty();

    }

    //==Helper Methods==//
    private Member createMember() {
        return Member.builder()
                .loginId("user1")
                .password("abc12345")
                .name("차태승")
                .email("user1@naver.com")
                .build();
    }

    private List<Member> createMembersWithDuplicateName() {
        List<Member> members = new ArrayList<>();
        members.add(createMember());
        members.add(Member.builder()
                .loginId("user2")
                .password("son456")
                .name("손흥민")
                .email("user2@google.com")
                .build());
        members.add(Member.builder()
                .loginId("user3")
                .password("abc12345")
                .name("차태승")
                .email("user3@naver.com")
                .build());
        return members;
    }
}