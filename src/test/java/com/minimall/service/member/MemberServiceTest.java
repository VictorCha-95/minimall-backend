package com.minimall.service.member;

import com.minimall.domain.common.DomainType;
import com.minimall.domain.embeddable.Address;
import com.minimall.domain.member.Member;
import com.minimall.domain.member.MemberRepository;
import com.minimall.api.member.dto.request.MemberUpdateRequest;
import com.minimall.domain.exception.DuplicateException;
import com.minimall.service.exception.InvalidCredentialException;
import com.minimall.service.exception.MemberNotFoundException;
import com.minimall.service.member.dto.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MemberServiceTest {

    @Mock
    MemberServiceMapper memberServiceMapper;

    @Mock
    MemberRepository memberRepository;

    @Mock
    PasswordEncoder passwordEncoder;

    @InjectMocks
    MemberService memberService;

    private Member member;
    private MemberCreateCommand createCommand;
    private MemberUpdateRequest updateRequest;
    private MemberUpdateCommand updateCommand;
    private MemberDetailResult detailResult;
    private MemberSummaryResult summaryResult;


    @BeforeEach
    void setUp() {
        //== Member Entity ==//
        member = Member.builder()
                .loginId("user1")
                .password("abc12345")
                .name("차태승")
                .email("cts9458@naver.com")
                .addr(new Address("12345", "광주광역시", "광산구", "수등로76번길 40", "123동 1501호"))
                .build();

        //== CreateRequest DTO ==//
        createCommand = new MemberCreateCommand(member.getLoginId(), member.getPasswordHash(), member.getName(), member.getEmail(),
                new MemberAddressCommand(member.getAddr().getPostcode(), member.getAddr().getState(), member.getAddr().getCity(), member.getAddr().getStreet(), member.getAddr().getDetail()));

        //== UpdateRequest DTO ==//
        updateRequest = new MemberUpdateRequest(
                "newPassword456",
                "차태승2",
                "cts9458+update@naver.com",
                member.getAddr()
        );

        updateCommand = new MemberUpdateCommand(
                "newPassword456",
                "차태승2",
                "cts9458+update@naver.com",
                member.getAddr()
        );

        //== Response DTOs ==//
        summaryResult = new MemberSummaryResult(member.getId(), member.getLoginId(), member.getName());

        detailResult = new MemberDetailResult(member.getId(), member.getLoginId(), member.getName(), member.getEmail(), member.getGrade(), member.getAddr());
    }

    //== login ==//
    @Nested
    @DisplayName("login(MemberLoginCommand)")
    class Login {
        @Test
        @DisplayName("로그인 성공 -> DB 회원 조회 및 비밀번호 검증")
        void success() {
            //given
            when(memberRepository.findByLoginId(createCommand.loginId())).thenReturn(Optional.of(member));
            when(passwordEncoder.matches("abc12345", member.getPasswordHash())).thenReturn(true);

            //when
            Member result = memberService.login(new MemberLoginCommand(createCommand.loginId(), createCommand.password()));

            //then
            assertThat(result).isEqualTo(member);
            verify(memberRepository).findByLoginId(createCommand.loginId());
            verify(passwordEncoder).matches("abc12345", member.getPasswordHash());
        }

        @Test
        @DisplayName("비밀번호 오류 -> InvalidCredentialException 예외 발생")
        void shouldFail_whenPasswordIsNotMatch() {
            //given
            when(memberRepository.findByLoginId(createCommand.loginId())).thenReturn(Optional.of(member));
            when(passwordEncoder.matches("wrong_password", member.getPasswordHash())).thenReturn(false);

            //when & then
            assertThatThrownBy(() -> memberService.login(new MemberLoginCommand(createCommand.loginId(), "wrong_password")))
                    .isInstanceOf(InvalidCredentialException.class);

            verify(memberRepository).findByLoginId(createCommand.loginId());
            verify(passwordEncoder).matches("wrong_password", member.getPasswordHash());
        }

        @Test
        @DisplayName("회원 아이디 오류 -> MemberNotFound 예외 발생")
        void shouldFail_whenMemberIsNotFound() {
            //given
            when(memberRepository.findByLoginId("wrong_id")).thenReturn(Optional.empty());

            //when & then
            assertThatThrownBy(() -> memberService.login(new MemberLoginCommand("wrong_id", createCommand.password())))
                    .isInstanceOf(MemberNotFoundException.class);

            verify(memberRepository).findByLoginId("wrong_id");
        }

    }

    //== create ==//
    @Test
    void createMember_success() {
        //given
        when(memberRepository.existsByLoginId(createCommand.loginId())).thenReturn(false);
        when(memberRepository.existsByEmail(createCommand.email())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(memberServiceMapper.toEntity(any(MemberCreateCommand.class))).thenReturn(member);
        when(memberRepository.save(member)).thenReturn(member);

        //when
        Member result = memberService.create(createCommand);

        //then
        assertThat(result).isEqualTo(member);
        verify(memberRepository).existsByLoginId(createCommand.loginId());
        verify(memberRepository).existsByEmail(createCommand.email());
        verify(passwordEncoder).encode(anyString());
        verify(memberServiceMapper).toEntity(any(MemberCreateCommand.class));
        verify(memberRepository).save(member);
    }

    @Test
    @DisplayName("회원 등록 시 로그인 아이디 중복일 경우 예외 발생")
    void createMember_duplicateLoginId_shouldFail() {
        //given
        when(memberRepository.existsByLoginId(createCommand.loginId())).thenReturn(true);

        //then
        DuplicateException duplicateException =
                assertThrows(DuplicateException.class, () -> memberService.create(createCommand));
        assertThat(duplicateException.getMessage()).contains("loginId", "사용 중", member.getLoginId());
        verify(memberRepository).existsByLoginId(createCommand.loginId());
    }

    @Test
    @DisplayName("회원 등록 시 이메일 중복일 경우 예외 발생")
    void createMember_duplicateEmail_shouldFail() {
        //given
        when(memberRepository.existsByLoginId(createCommand.loginId())).thenReturn(false);
        when(memberRepository.existsByEmail(createCommand.email())).thenReturn(true);

        //then
        DuplicateException duplicateException =
                assertThrows(DuplicateException.class, () -> memberService.create(createCommand));
        assertThat(duplicateException.getMessage()).contains("email", "사용 중", member.getEmail());
        verify(memberRepository).existsByLoginId(createCommand.loginId());
        verify(memberRepository).existsByEmail(createCommand.email());
    }

    //== update ==//
    @Test
    void updateMember_success() {
        //given
        when(memberRepository.findByEmail(updateRequest.email())).thenReturn(Optional.empty());
        when(memberRepository.findById(1L)).thenReturn(Optional.of(member));
        when(memberServiceMapper.toDetailResult(member)).thenReturn(detailResult);

        //when
        MemberDetailResult result = memberService.update(1L, updateCommand);

        //then
        assertThat(result).isEqualTo(detailResult);
        verify(memberRepository).findByEmail(updateRequest.email());
        verify(memberRepository).findById(1L);
        verify(memberServiceMapper).toDetailResult(member);
    }

    @Test
    @DisplayName("회원 수정 시 기존 회원의 이메일과 중복된 이메일로 수정하면 예외 발생")
    void updateMember_duplicateEmail_shouldFail() {
        //given
        ReflectionTestUtils.setField(member, "id", 1L); // 수정 대상 회원
        Member existed = Member.create("other", "1234", "중복회원", updateRequest.email(), null);
        ReflectionTestUtils.setField(existed, "id", 2L); // 중복 이메일 보유자

        when(memberRepository.findByEmail(updateRequest.email())).thenReturn(Optional.of(existed));

        //when
        DuplicateException ex = assertThrows(DuplicateException.class,
                () -> memberService.update(1L, updateCommand)
        );

        //then
        assertThat(ex.getMessage()).contains("email", "사용 중");
    }




    @Test
    @DisplayName("회원 수정 시 회원id로 조회 불가능하면 예외 발생")
    void updateMember_notFindById_shouldFail() {
        //given
        when(memberRepository.findByEmail(updateRequest.email())).thenReturn(Optional.empty());
        when(memberRepository.findById(1L)).thenReturn(Optional.empty());

        //when
        MemberNotFoundException ex =
                assertThrows(MemberNotFoundException.class, () -> memberService.update(1L, updateCommand));

        //then
        assertThat(ex.getMessage())
                .isEqualTo(String.format("%s을(를) 찾을 수 없습니다. (%s: %s)",
                        DomainType.MEMBER.getDisPlayName(), "id", 1L));

        verify(memberRepository).findByEmail(updateRequest.email());
        verify(memberRepository).findById(1L);
    }


    //== delete ==//
    @Test
    void deleteMember_success() {
        //given
        when(memberRepository.findById(1L)).thenReturn(Optional.of(member));

        //when
        memberService.delete(1L);

        //then
        verify(memberRepository).findById(1L);
        verify(memberRepository).deleteById(1L);
    }

    @Test
    void deleteMember_notFindById_shouldFail() {
        //given
        when(memberRepository.findById(1L)).thenReturn(Optional.empty());

        //then
        MemberNotFoundException memberNotFoundException =
                assertThrows(MemberNotFoundException.class, () -> memberService.delete(1L));
        assertThat(memberNotFoundException.getMessage())
                .isEqualTo(String.format("%s을(를) 찾을 수 없습니다. (%s: %s)",
                        DomainType.MEMBER.getDisPlayName(),"id", 1L));
        verify(memberRepository).findById(1L);
    }

    @Test
    void getMembers() {
        //given
        when(memberRepository.findAll(Sort.by("id").ascending())).thenReturn(List.of(member));
        when(memberServiceMapper.toSummaryResult(member)).thenReturn(summaryResult);

        //when
        List<MemberSummaryResult> result = memberService.getMembers();

        //then
        assertThat(result).containsExactly(summaryResult);
        verify(memberRepository).findAll(Sort.by("id").ascending());
        verify(memberServiceMapper).toSummaryResult(member);
    }


}