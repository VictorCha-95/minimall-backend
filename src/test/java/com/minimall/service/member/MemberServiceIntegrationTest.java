package com.minimall.service.member;

import com.minimall.AbstractIntegrationTest;
import com.minimall.api.member.dto.request.MemberAddressRequest;
import com.minimall.api.member.dto.request.MemberCreateRequest;
import com.minimall.api.member.dto.request.MemberUpdateRequest;
import com.minimall.api.member.dto.response.MemberDetailWithOrdersResponse;
import com.minimall.domain.embeddable.Address;
import com.minimall.domain.member.Member;
import com.minimall.domain.member.MemberRepository;
import com.minimall.domain.exception.DuplicateException;
import com.minimall.domain.order.OrderRepository;
import com.minimall.service.exception.InvalidCredentialException;
import com.minimall.service.exception.MemberNotFoundException;
import com.minimall.service.exception.NotFoundException;
import com.minimall.service.member.dto.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
public class MemberServiceIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    MemberService memberService;

    @Autowired
    MemberRepository memberRepository;

    @Autowired
    OrderRepository orderRepository;

    @Autowired
    PasswordEncoder passwordEncoder;

    private Member member;
    private MemberCreateRequest createRequest;
    private MemberCreateCommand createCommand;
    private MemberUpdateRequest updateRequest;
    private MemberUpdateCommand updateCommand;

    private static final String DEFAULT_LOGIN_ID = "user123";
    private static final String DEFAULT_PASSWORD_HASH = "12345678";
    private static final String DEFAULT_NAME = "차태승";
    private static final String DEFAULT_EMAIL = "user123@example.com";
    private static final Address DEFAULT_ADDRESS =
            Address.createAddress("62550", "광주광역시", "광산구", "수등로76번길 40", "123동 456호");


    @BeforeEach
    void setUp() {
        //== Member Entity ==//
        member = Member.registerCustomer(DEFAULT_LOGIN_ID, DEFAULT_PASSWORD_HASH, DEFAULT_NAME, DEFAULT_EMAIL, DEFAULT_ADDRESS);

        //== CreateRequest DTO ==//
        createRequest = new MemberCreateRequest(member.getLoginId(), member.getPasswordHash(), member.getName(), member.getEmail(),
                new MemberAddressRequest(member.getAddr().getPostcode(), member.getAddr().getState(), member.getAddr().getCity(), member.getAddr().getStreet(), member.getAddr().getDetail()));

        //== CreateCommand DTO ==//
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

        orderRepository.deleteAll();
        memberRepository.deleteAll();
    }

    @Nested
    @DisplayName("login(MemberLoginCommand)")
    class Login {
        @Test
        @DisplayName("로그인 성공: DB 회원 조회 -> 비밀번호 검증")
        void success() {
            //given
            Member member = memberService.createCustomer(createCommand);
            MemberLoginCommand command = new MemberLoginCommand(createCommand.loginId(), createCommand.password());

            //when
            Member result = memberService.login(command);

            //then
            assertThat(result).isEqualTo(member);
        }

        @Test
        @DisplayName("비밀번호 오류 -> InvalidCredentialException 예외 발생")
        void shouldFail_whenPasswordIsNotMatch() {
            //given
            memberService.createCustomer(createCommand);
            MemberLoginCommand command = new MemberLoginCommand(createCommand.loginId(), "wrong_password");

            //when & then
            assertThatThrownBy(() -> memberService.login(command))
                    .isInstanceOf(InvalidCredentialException.class);
        }

        @Test
        @DisplayName("회원 아이디 오류 -> MemberNotFound 예외 발생")
        void shouldFail_whenMemberIsNotFound() {
            //given
            memberService.createCustomer(createCommand);
            MemberLoginCommand command = new MemberLoginCommand("wrong_loginId", createCommand.password());

            //when & then
            assertThatThrownBy(() -> memberService.login(command))
                    .isInstanceOf(MemberNotFoundException.class);
        }
    }


    @Nested
    @DisplayName("회원 생성")
    class Create{
        @Test
        void createMember_success() {
            //when
            Member created = memberService.createCustomer(createCommand);

            //then
            MemberSummaryResult found = memberService.getSummary(created.getId());
            assertThat(found.name()).isEqualTo(created.getName());
        }

        @Test
        void createMember_duplicateLoginId_shouldFail() {
            //given
            memberService.createCustomer(createCommand);
            MemberCreateCommand duplicateLoginIdCommand =
                    new MemberCreateCommand(member.getLoginId(), member.getPasswordHash(), "차태승", "example@naver.com", null);

            //then
            assertThrows(DuplicateException.class, () -> memberService.createCustomer(duplicateLoginIdCommand));
        }

        @Test
        void createMember_duplicateEmail_shouldFail() {
            //given
            memberService.createCustomer(createCommand);
            MemberCreateCommand duplicateEmailCommand =
                    new MemberCreateCommand("exampleLoginId", member.getPasswordHash(), "차태승", member.getEmail(), null);

            //then
            assertThrows(DuplicateException.class, () -> memberService.createCustomer(duplicateEmailCommand));
        }

        @Test
        void createMember_encryptPassword(){
            //given
            MemberCreateCommand command = new MemberCreateCommand(
                    "login123",
                    "plainPassword",  // 평문
                    "손흥민",
                    "son@example.com",
                    null
            );

            //when
            Member member = memberService.createCustomer(command);

            //then
            assertThat(member.getPasswordHash()).isNotEqualTo("plainPassword");
            assertThat(passwordEncoder.matches("plainPassword", member.getPasswordHash())).isTrue();
        }
    }

    @Nested
    @DisplayName("회원 수정")
    class Update {

        @Test
        void updateMember_success() {
            //given
            Member created = memberService.createCustomer(createCommand);

            //when
            MemberDetailResult updated = memberService.update(created.getId(), updateCommand);
            MemberDetailResult found = memberService.getDetail(updated.id());

            //then
            assertThat(found.id()).isEqualTo(created.getId());
            assertThat(found.name()).isEqualTo(updateRequest.name());
            assertThat(found.email()).isEqualTo(updateRequest.email());
        }

        @Test
        void updateMember_duplicateEmail_shouldFail() {
            //given
            Member original = memberService.createCustomer(createCommand);
            MemberDetailResult foundOriginal = memberService.getDetail(original.getId());
            Member newCreated = memberService.createCustomer(new MemberCreateCommand(
                    "example123", "12345", "이름", "example123@naver.com", null));

            //then
            assertThrows(DuplicateException.class,
                    () -> memberService.update(newCreated.getId(),
                            new MemberUpdateCommand(null, null, foundOriginal.email(), null)));
        }
    }

    @Nested
    @DisplayName("회원 삭제")
    class Delete {

        @Test
        void deleteMember_success() {
            //given
            Member created = memberService.createCustomer(createCommand);

            //then
            memberService.delete(created.getId());
        }

        @Test
        void deleteMember_deletedMemberFind_shouldFail() {
            //given
            Member created = memberService.createCustomer(createCommand);

            //when
            memberService.delete(created.getId());

            //then
            assertThrows(NotFoundException.class,
                    () -> memberService.getSummary(created.getId()));
        }
    }

    @Nested
    @DisplayName("회원 조회")
    class GetMember{

        @Test
        void getMemberSummary_success() {
            //given
            Member created = memberService.createCustomer(createCommand);

            //when
            MemberSummaryResult memberSummary = memberService.getSummary(created.getId());

            //then
            assertThat(memberSummary.id()).isEqualTo(created.getId());
            assertThat(memberSummary.loginId()).isEqualTo(created.getLoginId());
            assertThat(memberSummary.name()).isEqualTo(created.getName());
        }

        @Test
        void getMemberDetail_success() {
            //given
            Member created = memberService.createCustomer(createCommand);

            //when
            MemberDetailResult memberDetail = memberService.getDetail(created.getId());

            //then
            assertThat(memberDetail.id()).isEqualTo(created.getId());
            assertThat(memberDetail.loginId()).isEqualTo(created.getLoginId());
            assertThat(memberDetail.name()).isEqualTo(created.getName());
        }

        @Test
        void getMemberDetailWithOrders_success() {
            //given
            Member created = memberService.createCustomer(createCommand);

            //when
            MemberDetailWithOrdersResponse memberDetailWithOrders = memberService.getDetailWithOrders(created.getId());

            //then
            assertThat(memberDetailWithOrders.id()).isEqualTo(created.getId());
            assertThat(memberDetailWithOrders.loginId()).isEqualTo(created.getLoginId());
            assertThat(memberDetailWithOrders.name()).isEqualTo(created.getName());
        }

        @Test
        void getMemberSummaryByEmail_success() {
            //given
            Member created = memberService.createCustomer(createCommand);

            //when
            MemberSummaryResult memberSummaryByEmail = memberService.getSummaryByEmail(createRequest.email());

            //then
            assertThat(memberSummaryByEmail.id()).isEqualTo(created.getId());
            assertThat(memberSummaryByEmail.loginId()).isEqualTo(created.getLoginId());
            assertThat(memberSummaryByEmail.name()).isEqualTo(created.getName());
        }

        @Test
        void getMemberDetailByEmail_success() {
            //given
            Member created = memberService.createCustomer(createCommand);

            //when
            MemberDetailResult memberDetailByEmail = memberService.getDetailByEmail(createRequest.email());

            //then
            assertThat(memberDetailByEmail.id()).isEqualTo(created.getId());
            assertThat(memberDetailByEmail.loginId()).isEqualTo(created.getLoginId());
            assertThat(memberDetailByEmail.name()).isEqualTo(created.getName());
        }

        @Test
        void getMemberSummaryByLoginId_success() {
            //given
            Member created = memberService.createCustomer(createCommand);

            //when
            MemberSummaryResult memberSummaryByLoginId = memberService.getSummaryByLoginId(created.getLoginId());

            //then
            assertThat(memberSummaryByLoginId.id()).isEqualTo(created.getId());
            assertThat(memberSummaryByLoginId.loginId()).isEqualTo(created.getLoginId());
            assertThat(memberSummaryByLoginId.name()).isEqualTo(created.getName());
        }

        @Test
        void getMemberDetailByLoginId_success() {
            //given
            Member created = memberService.createCustomer(createCommand);

            //when
            MemberDetailResult memberDetailByLoginId = memberService.getDetailByLoginId(created.getLoginId());

            //then
            assertThat(memberDetailByLoginId.id()).isEqualTo(created.getId());
            assertThat(memberDetailByLoginId.loginId()).isEqualTo(created.getLoginId());
            assertThat(memberDetailByLoginId.name()).isEqualTo(created.getName());
        }

        @Test
        void getMembers() {
            //given
            memberService.createCustomer(createCommand);

            //when
            List<MemberSummaryResult> result = memberService.getMembers();

            //then
            assertThat(result.size()).isEqualTo(1);
        }

    }

}
