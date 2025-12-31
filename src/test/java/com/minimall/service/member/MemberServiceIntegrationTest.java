package com.minimall.service.member;

import com.minimall.AbstractIntegrationTest;
import com.minimall.api.member.dto.request.MemberAddressRequest;
import com.minimall.api.member.dto.request.MemberRegisterRequest;
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
import com.minimall.service.member.dto.command.MemberAddressCommand;
import com.minimall.service.member.dto.command.MemberLoginCommand;
import com.minimall.service.member.dto.command.MemberRegisterCommand;
import com.minimall.service.member.dto.command.MemberUpdateCommand;
import com.minimall.service.member.dto.result.MemberDetailResult;
import com.minimall.service.member.dto.result.MemberSummaryResult;
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
    private MemberRegisterRequest createRequest;
    private MemberRegisterCommand createCommand;
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
        createRequest = new MemberRegisterRequest(member.getLoginId(), member.getPasswordHash(), member.getName(), member.getEmail(),
                new MemberAddressRequest(member.getAddr().getPostcode(), member.getAddr().getState(), member.getAddr().getCity(), member.getAddr().getStreet(), member.getAddr().getDetail()));

        //== CreateCommand DTO ==//
        createCommand = new MemberRegisterCommand(member.getLoginId(), member.getPasswordHash(), member.getName(), member.getEmail(),
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
            MemberSummaryResult member = memberService.registerCustomer(createCommand);
            MemberLoginCommand command = new MemberLoginCommand(createCommand.loginId(), createCommand.password());

            //when
            MemberSummaryResult result = memberService.login(command);

            //then
            assertThat(result).isEqualTo(member);
        }

        @Test
        @DisplayName("비밀번호 오류 -> InvalidCredentialException 예외 발생")
        void shouldFail_whenPasswordIsNotMatch() {
            //given
            memberService.registerCustomer(createCommand);
            MemberLoginCommand command = new MemberLoginCommand(createCommand.loginId(), "wrong_password");

            //when & then
            assertThatThrownBy(() -> memberService.login(command))
                    .isInstanceOf(InvalidCredentialException.class);
        }

        @Test
        @DisplayName("회원 아이디 오류 -> MemberNotFound 예외 발생")
        void shouldFail_whenMemberIsNotFound() {
            //given
            memberService.registerCustomer(createCommand);
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
            MemberSummaryResult created = memberService.registerCustomer(createCommand);

            //then
            MemberSummaryResult found = memberService.getSummary(created.id());
            assertThat(found.name()).isEqualTo(created.name());
        }

        @Test
        void createMember_duplicateLoginId_shouldFail() {
            //given
            memberService.registerCustomer(createCommand);
            MemberRegisterCommand duplicateLoginIdCommand =
                    new MemberRegisterCommand(member.getLoginId(), member.getPasswordHash(), "차태승", "example@naver.com", null);

            //then
            assertThrows(DuplicateException.class, () -> memberService.registerCustomer(duplicateLoginIdCommand));
        }

        @Test
        void createMember_duplicateEmail_shouldFail() {
            //given
            memberService.registerCustomer(createCommand);
            MemberRegisterCommand duplicateEmailCommand =
                    new MemberRegisterCommand("exampleLoginId", member.getPasswordHash(), "차태승", member.getEmail(), null);

            //then
            assertThrows(DuplicateException.class, () -> memberService.registerCustomer(duplicateEmailCommand));
        }

        @Test
        void createMember_encryptPassword(){
            //given
            MemberRegisterCommand command = new MemberRegisterCommand(
                    "login123",
                    "plainPassword",  // 평문
                    "손흥민",
                    "son@example.com",
                    null
            );

            //when
            MemberSummaryResult result = memberService.registerCustomer(command);
            Member member = memberRepository.findById(result.id()).get();

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
            MemberSummaryResult created = memberService.registerCustomer(createCommand);

            //when
            MemberDetailResult updated = memberService.update(created.id(), updateCommand);
            MemberDetailResult found = memberService.getDetail(updated.id());

            //then
            assertThat(found.id()).isEqualTo(created.id());
            assertThat(found.name()).isEqualTo(updateRequest.name());
            assertThat(found.email()).isEqualTo(updateRequest.email());
        }

        @Test
        void updateMember_duplicateEmail_shouldFail() {
            //given
            MemberSummaryResult original = memberService.registerCustomer(createCommand);
            MemberDetailResult foundOriginal = memberService.getDetail(original.id());
            MemberSummaryResult newCreated = memberService.registerCustomer(new MemberRegisterCommand(
                    "example123", "12345", "이름", "example123@naver.com", null));

            //then
            assertThrows(DuplicateException.class,
                    () -> memberService.update(newCreated.id(),
                            new MemberUpdateCommand(null, null, foundOriginal.email(), null)));
        }
    }

    @Nested
    @DisplayName("회원 삭제")
    class Delete {

        @Test
        void deleteMember_success() {
            //given
            MemberSummaryResult created = memberService.registerCustomer(createCommand);

            //then
            memberService.delete(created.id());
        }

        @Test
        void deleteMember_deletedMemberFind_shouldFail() {
            //given
            MemberSummaryResult created = memberService.registerCustomer(createCommand);

            //when
            memberService.delete(created.id());

            //then
            assertThrows(NotFoundException.class,
                    () -> memberService.getSummary(created.id()));
        }
    }

    @Nested
    @DisplayName("회원 조회")
    class GetMember{

        @Test
        void getMemberSummary_success() {
            //given
            MemberSummaryResult created = memberService.registerCustomer(createCommand);

            //when
            MemberSummaryResult memberSummary = memberService.getSummary(created.id());

            //then
            assertThat(memberSummary.id()).isEqualTo(created.id());
            assertThat(memberSummary.loginId()).isEqualTo(created.loginId());
            assertThat(memberSummary.name()).isEqualTo(created.name());
        }

        @Test
        void getMemberDetail_success() {
            //given
            MemberSummaryResult created = memberService.registerCustomer(createCommand);

            //when
            MemberDetailResult memberDetail = memberService.getDetail(created.id());

            //then
            assertThat(memberDetail.id()).isEqualTo(created.id());
            assertThat(memberDetail.loginId()).isEqualTo(created.loginId());
            assertThat(memberDetail.name()).isEqualTo(created.name());
        }

        @Test
        void getMemberDetailWithOrders_success() {
            //given
            MemberSummaryResult created = memberService.registerCustomer(createCommand);

            //when
            MemberDetailWithOrdersResponse memberDetailWithOrders = memberService.getDetailWithOrders(created.id());

            //then
            assertThat(memberDetailWithOrders.id()).isEqualTo(created.id());
            assertThat(memberDetailWithOrders.loginId()).isEqualTo(created.loginId());
            assertThat(memberDetailWithOrders.name()).isEqualTo(created.name());
        }

        @Test
        void getMemberSummaryByEmail_success() {
            //given
            MemberSummaryResult created = memberService.registerCustomer(createCommand);

            //when
            MemberSummaryResult memberSummaryByEmail = memberService.getSummaryByEmail(createRequest.email());

            //then
            assertThat(memberSummaryByEmail.id()).isEqualTo(created.id());
            assertThat(memberSummaryByEmail.loginId()).isEqualTo(created.loginId());
            assertThat(memberSummaryByEmail.name()).isEqualTo(created.name());
        }

        @Test
        void getMemberDetailByEmail_success() {
            //given
            MemberSummaryResult created = memberService.registerCustomer(createCommand);

            //when
            MemberDetailResult memberDetailByEmail = memberService.getDetailByEmail(createRequest.email());

            //then
            assertThat(memberDetailByEmail.id()).isEqualTo(created.id());
            assertThat(memberDetailByEmail.loginId()).isEqualTo(created.loginId());
            assertThat(memberDetailByEmail.name()).isEqualTo(created.name());
        }

        @Test
        void getMemberSummaryByLoginId_success() {
            //given
            MemberSummaryResult created = memberService.registerCustomer(createCommand);

            //when
            MemberSummaryResult memberSummaryByLoginId = memberService.getSummaryByLoginId(created.loginId());

            //then
            assertThat(memberSummaryByLoginId.id()).isEqualTo(created.id());
            assertThat(memberSummaryByLoginId.loginId()).isEqualTo(created.loginId());
            assertThat(memberSummaryByLoginId.name()).isEqualTo(created.name());
        }

        @Test
        void getMemberDetailByLoginId_success() {
            //given
            MemberSummaryResult created = memberService.registerCustomer(createCommand);

            //when
            MemberDetailResult memberDetailByLoginId = memberService.getDetailByLoginId(created.loginId());

            //then
            assertThat(memberDetailByLoginId.id()).isEqualTo(created.id());
            assertThat(memberDetailByLoginId.loginId()).isEqualTo(created.loginId());
            assertThat(memberDetailByLoginId.name()).isEqualTo(created.name());
        }

        @Test
        void getMembers() {
            //given
            memberService.registerCustomer(createCommand);

            //when
            List<MemberSummaryResult> result = memberService.getMembers();

            //then
            assertThat(result.size()).isEqualTo(1);
        }

    }

}
