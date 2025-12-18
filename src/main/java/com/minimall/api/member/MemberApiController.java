package com.minimall.api.member;

import com.minimall.api.member.dto.MemberApiMapper;
import com.minimall.api.member.dto.request.MemberCreateRequest;
import com.minimall.api.member.dto.request.MemberLoginRequest;
import com.minimall.api.member.dto.request.MemberUpdateRequest;
import com.minimall.api.member.dto.response.MemberDetailResponse;
import com.minimall.api.member.dto.response.MemberDetailWithOrdersResponse;
import com.minimall.api.member.dto.response.MemberSummaryResponse;
import com.minimall.api.order.dto.OrderApiMapper;
import com.minimall.api.order.dto.response.OrderSummaryResponse;
import com.minimall.domain.member.Member;
import com.minimall.service.member.MemberService;
import com.minimall.service.member.dto.MemberUpdateCommand;
import com.minimall.service.order.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/members", produces = "application/json")
@Tag(name = "Member API", description = "회원 관련 API")
public class MemberApiController {

    private final MemberService memberService;
    private final OrderService orderService;
    private final MemberApiMapper memberApiMapper;
    private final OrderApiMapper orderApiMapper;

    //== 로그인 ==//
    @Operation(summary = "회원 로그인")
    @PostMapping("/login")
    public ResponseEntity<MemberSummaryResponse> login(@RequestBody @Valid MemberLoginRequest request){
        Member member = memberService.login(memberApiMapper.toLoginCommand(request));
        MemberSummaryResponse response = memberApiMapper.toSummaryResponse(member);
        return ResponseEntity.ok(response);
    }

    //== 회원 조회 ==//
    @Operation(summary = "회원 전체 조회", description = "모든 회원 요약 조회")
    @GetMapping
    public List<MemberSummaryResponse> getAll() {
        return memberService.getMembers().stream()
                .map(memberApiMapper::toSummaryResponse)
                .toList();
    }

    @Operation(summary = "회원 단건 상세 조회", description = "회원 ID로 상세 조회")
    @GetMapping("/{id}")
    public MemberDetailResponse getDetail(@PathVariable Long id) {
        return memberApiMapper.toDetailResponse(memberService.getDetail(id));
    }

    @Operation(summary = "회원 단건 요약 조회", description = "회원 ID로 요약 조회")
    @GetMapping("/{id}/summary")
    public MemberSummaryResponse getSummary(@PathVariable Long id) {
        return memberApiMapper.toSummaryResponse(memberService.getSummary(id));
    }

    @Operation(summary = "회원 단건 상세 조회(주문 포함)", description = "회원 ID로 상세 조회하며 주문내역 포함")
    @GetMapping("/{id}/with-orders")
    public MemberDetailWithOrdersResponse getDetailWithOrders(@PathVariable Long id) {
        return memberService.getDetailWithOrders(id);
    }

    @Operation(summary = "회원 상세 조회 by Email", description = "이메일로 회원 상세 조회")
    @GetMapping("/by-email")
    public MemberDetailResponse getDetailByEmail(@RequestParam String email) {
        return memberApiMapper.toDetailResponse(memberService.getDetailByEmail(email));
    }

    @Operation(summary = "회원 요약 조회 by Email", description = "이메일로 회원 요약 조회")
    @GetMapping("/by-email/summary")
    public MemberSummaryResponse getSummaryByEmail(@RequestParam String email) {
        return memberApiMapper.toSummaryResponse(memberService.getSummaryByEmail(email));
    }

    @Operation(summary = "회원 상세 조회 by LoginId", description = "로그인 ID로 회원 상세 조회")
    @GetMapping("/by-loginId")
    public MemberDetailResponse getDetailByLoginId(@RequestParam String loginId) {
        return memberApiMapper.toDetailResponse(memberService.getDetailByLoginId(loginId));
    }

    @Operation(summary = "회원 요약 조회 by LoginId", description = "로그인 ID로 회원 요약 조회")
    @GetMapping("/by-loginId/summary")
    public MemberSummaryResponse getSummaryByLoginId(@RequestParam String loginId) {
        return memberApiMapper.toSummaryResponse(memberService.getSummaryByLoginId(loginId));
    }

    //== 주문 조회 ==//
    @Operation(summary = "주문 요약 목록 조회")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "400", description = "요청 검증 오류"),
    })
    @GetMapping("/{id}/orders")
    public ResponseEntity<List<OrderSummaryResponse>> getOrdersByMember(@PathVariable Long id) {
        List<OrderSummaryResponse> response = orderService.getOrderSummaries(id).stream()
                .map(orderApiMapper::toOrderSummaryResponse)
                .toList();
        return ResponseEntity.ok(response);
    }

    //== 회원 생성 ==//
    @Operation(summary = "회원 생성")
    @PostMapping
    public MemberSummaryResponse create(@Valid @RequestBody MemberCreateRequest request) {
        Member member = memberService.create(memberApiMapper.toCreateCommand(request));
        return memberApiMapper.toSummaryResponse(member);
    }

    //== 회원 수정 ==//
    @Operation(summary = "회원 수정", description = "기존 회원 정보 수정")
    @PatchMapping("/{id}")
    public MemberDetailResponse update(@PathVariable Long id,
                                       @Valid @RequestBody MemberUpdateRequest request) {
        MemberUpdateCommand command = memberApiMapper.toUpdateCommand(request);
        return memberApiMapper.toDetailResponse(memberService.update(id, command));
    }

    //== 회원 삭제 ==//
    @Operation(summary = "회원 삭제", description = "기존 회원 삭제")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        memberService.delete(id);
        return ResponseEntity.noContent().build();
    }
}