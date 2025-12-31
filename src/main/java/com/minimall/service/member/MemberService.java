package com.minimall.service.member;

import com.minimall.api.member.dto.response.MemberDetailWithOrdersResponse;
import com.minimall.domain.embeddable.Address;
import com.minimall.domain.member.Member;
import com.minimall.domain.member.MemberRepository;
import com.minimall.domain.exception.DuplicateException;
import com.minimall.domain.member.Role;
import com.minimall.service.exception.InvalidCredentialException;
import com.minimall.service.exception.MemberNotFoundException;
import com.minimall.service.member.dto.*;
import com.minimall.service.member.dto.command.*;
import com.minimall.service.member.dto.result.MemberDetailResult;
import com.minimall.service.member.dto.result.MemberSummaryResult;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberService {

    private final MemberRepository memberRepository;
    private final MemberServiceMapper memberServiceMapper;
    private final PasswordEncoder passwordEncoder;

    //== 로그인 ==//
    @Transactional
    public MemberSummaryResult login(MemberLoginCommand command) {
        Member member = findMemberByLoginId(command.loginId());
        if (!passwordEncoder.matches(command.password(), member.getPasswordHash())){
            throw new InvalidCredentialException("password not matched");
        }

        return memberServiceMapper.toSummaryResult(member);
    }

    //== 생성 ==//
    @Transactional
    public MemberSummaryResult registerCustomer(MemberRegisterCommand command) {
        validateDuplicateLoginId(command.loginId());
        validateDuplicateEmail(command.email());

        String passwordHash = passwordEncoder.encode(command.password());
        Address addr = getAddress(command.addr());

        Member member = Member.registerCustomer(command.loginId(), passwordHash, command.name(), command.email(), addr);
        Member saved = memberRepository.save(member);
        return memberServiceMapper.toSummaryResult(saved);
    }

    @Transactional
    public MemberSummaryResult registerSeller(SellerRegisterCommand command) {
        validateDuplicateLoginId(command.loginId());
        validateDuplicateEmail(command.email());

        String passwordHash = passwordEncoder.encode(command.password());
        Address addr = getAddress(command.addr());

        Member member = Member.registerSeller(command.loginId(), passwordHash, command.name(), command.email(), addr,
                command.storeName(), command.businessNumber(), command.account());
        Member saved = memberRepository.save(member);
        return memberServiceMapper.toSummaryResult(saved);
    }

    @Transactional
    public MemberSummaryResult registerAdmin(MemberRegisterCommand command) {
        validateDuplicateLoginId(command.loginId());
        validateDuplicateEmail(command.email());

        String passwordHash = passwordEncoder.encode(command.password());
        Address addr = getAddress(command.addr());

        Member member = Member.registerCustomer(command.loginId(), passwordHash, command.name(), command.email(), addr);
        Member saved = memberRepository.save(member);
        return memberServiceMapper.toSummaryResult(saved);
    }

    private Address getAddress(MemberAddressCommand command) {
        Address address = null;
        if (command != null) {
            address = new Address(
                    command.postcode(),
                    command.state(),
                    command.city(),
                    command.street(),
                    command.detail()
            );
        }

        return address;
    }

    //== 수정 ==//
    @Transactional
    public MemberDetailResult update(Long memberId, MemberUpdateCommand command) {
        validateDuplicateEmailForUpdate(memberId, command.email());
        //TODO 비밀번호 검증, 암호화 로직 추가
        Member member = findMemberById(memberId);
        member.update(command.password(), command.name(), command.email(), command.addr());
        return memberServiceMapper.toDetailResult(member);
    }


    //== 삭제 ==//
    //TODO Soft delete 및 deleteException 고려
    @Transactional
    public void delete(Long memberId) {
        findMemberById(memberId);
        memberRepository.deleteById(memberId);
    }

    //== 단건 조회 ==//
    public MemberSummaryResult getSummary(Long memberId) {
        return memberServiceMapper.toSummaryResult(findMemberById(memberId));
    }
    public MemberDetailResult getDetail(Long memberId) {
        return memberServiceMapper.toDetailResult(findMemberById(memberId));
    }

    public MemberDetailWithOrdersResponse getDetailWithOrders(Long memberId) {
        return memberServiceMapper.toDetailWithOrdersResponse(findMemberById(memberId));
    }

    public MemberSummaryResult getSummaryByEmail(String email) {
        return memberServiceMapper.toSummaryResult(findMemberByEmail(email));
    }

    public MemberDetailResult getDetailByEmail(String email) {
        return memberServiceMapper.toDetailResult(findMemberByEmail(email));
    }

    public MemberSummaryResult getSummaryByLoginId(String loginId) {
        return memberServiceMapper.toSummaryResult(findMemberByLoginId(loginId));
    }


    public MemberDetailResult getDetailByLoginId(String loginId) {
        return memberServiceMapper.toDetailResult(findMemberByLoginId(loginId));
    }


    //== 목록 조회==//
    public List<MemberSummaryResult> getMembers() {
        //TODO searchDto를 인자로 받아 pageable 등 구현
        return memberRepository.findAll(Sort.by(Sort.Direction.ASC, "id"))
                .stream()
                .map(memberServiceMapper::toSummaryResult)
                .toList();
    }

    //== 검증 로직 ==//
    private void validateDuplicateLoginId(String loginId) {
        if (memberRepository.existsByLoginId(loginId)) {
            DuplicateException.validateField("loginId", loginId);
        }
    }
    private void validateDuplicateEmail(String email) {
        if (memberRepository.existsByEmail(email)) {
            DuplicateException.validateField("email", email);
        }
    }

    private void validateDuplicateEmailForUpdate(Long memberId, String email) {
        memberRepository.findByEmail(email)
                .filter(m -> !m.getId().equals(memberId))
                .ifPresent(m -> DuplicateException.validateField("email", email));
    }


    //== 공통 조회 메서드 ==//
    private Member findMemberById(Long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new MemberNotFoundException("id", memberId));
    }

    private Member findMemberByEmail(String email) {
        return memberRepository.findByEmail(email)
                .orElseThrow(() -> new MemberNotFoundException("email", email));
    }

    private Member findMemberByLoginId(String loginId) {
        return memberRepository.findByLoginId(loginId)
                .orElseThrow(() -> new MemberNotFoundException("loginId", loginId));
    }
}