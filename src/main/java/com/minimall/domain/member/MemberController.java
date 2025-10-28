package com.minimall.domain.member;

import com.minimall.domain.member.dto.request.MemberCreateRequestDto;
import com.minimall.domain.member.dto.request.MemberUpdateRequestDto;
import com.minimall.domain.member.dto.response.MemberDetailResponseDto;
import com.minimall.domain.member.dto.response.MemberDetailWithOrdersResponseDto;
import com.minimall.domain.member.dto.response.MemberSummaryResponseDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/members")
@Tag(name = "Member API", description = "회원 관련 API")
public class MemberController {

    private final MemberService memberService;

    //== 조회 ==//
    @Operation(summary = "회원 전체 조회", description = "모든 회원 요약 조회")
    @GetMapping
    public List<MemberSummaryResponseDto> getAll() {
        return memberService.getMembers();
    }

    @Operation(summary = "회원 단건 상세 조회", description = "회원 ID로 상세 조회")
    @GetMapping("/{id}")
    public MemberDetailResponseDto getDetail(@PathVariable Long id) {
        return memberService.getDetail(id);
    }

    @Operation(summary = "회원 단건 요약 조회", description = "회원 ID로 요약 조회")
    @GetMapping("/{id}/summary")
    public MemberSummaryResponseDto getSummary(@PathVariable Long id) {
        return memberService.getSummary(id);
    }

    @Operation(summary = "회원 단건 상세 조회(주문 포함)", description = "회원 ID로 상세 조회하며 주문내역 포함")
    @GetMapping("/{id}/with-orders")
    public MemberDetailWithOrdersResponseDto getDetailWithOrders(@PathVariable Long id) {
        return memberService.getDetailWithOrders(id);
    }

    @Operation(summary = "회원 상세 조회 by Email", description = "이메일로 회원 상세 조회")
    @GetMapping("/by-email")
    public MemberDetailResponseDto getDetailByEmail(@RequestParam String email) {
        return memberService.getDetailByEmail(email);
    }

    @Operation(summary = "회원 요약 조회 by Email", description = "이메일로 회원 요약 조회")
    @GetMapping("/by-email/summary")
    public MemberSummaryResponseDto getSummaryByEmail(@RequestParam String email) {
        return memberService.getSummaryByEmail(email);
    }

    @Operation(summary = "회원 상세 조회 by LoginId", description = "로그인 ID로 회원 상세 조회")
    @GetMapping("/by-loginId")
    public MemberDetailResponseDto getDetailByLoginId(@RequestParam String loginId) {
        return memberService.getDetailByLoginId(loginId);
    }

    @Operation(summary = "회원 요약 조회 by LoginId", description = "로그인 ID로 회원 요약 조회")
    @GetMapping("/by-loginId/summary")
    public MemberSummaryResponseDto getSummaryByLoginId(@RequestParam String loginId) {
        return memberService.getSummaryByLoginId(loginId);
    }

    //== 생성 ==//
    @Operation(summary = "회원 생성", description = "새로운 회원 생성")
    @PostMapping
    public MemberSummaryResponseDto create(@RequestBody MemberCreateRequestDto request) {
        return memberService.create(request);
    }

    //== 수정 ==//
    @Operation(summary = "회원 수정", description = "기존 회원 정보 수정")
    @PatchMapping("/{id}")
    public MemberDetailResponseDto update(@PathVariable Long id, @RequestBody MemberUpdateRequestDto request) {
        return memberService.update(id, request);
    }

    //== 삭제 ==//
    @Operation(summary = "회원 삭제", description = "기존 회원 삭제")
    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        memberService.delete(id);
    }
}