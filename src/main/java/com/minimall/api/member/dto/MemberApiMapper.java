package com.minimall.api.member.dto;

import com.minimall.api.member.dto.request.MemberLoginRequest;
import com.minimall.api.member.dto.request.MemberUpdateRequest;
import com.minimall.api.member.dto.response.MemberSummaryResponse;
import com.minimall.api.order.dto.OrderApiMapper;
import com.minimall.domain.member.Member;
import com.minimall.api.member.dto.request.MemberCreateRequest;
import com.minimall.api.member.dto.response.MemberDetailResponse;
import com.minimall.api.member.dto.response.MemberDetailWithOrdersResponse;
import com.minimall.service.member.dto.*;
import jakarta.validation.Valid;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring", uses = OrderApiMapper.class, unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface MemberApiMapper {

    // ===== Request -> Command (API → Service) ===== //

    // == Login == //
    MemberLoginCommand toLoginCommand(MemberLoginRequest request);

    //== Create ==//
    @Mapping(target = "withEncodedPassword", ignore = true)
    MemberCreateCommand toCreateCommand(MemberCreateRequest request);

    //== Update ==//
    @Mapping(target = "orders", ignore = true)
    void updateFromRequest(@MappingTarget Member member, MemberUpdateRequest request);

    MemberUpdateCommand toUpdateCommand(@Valid MemberUpdateRequest request);

    //== Response ==//
    @Mapping(target = "grade", expression = "java(member.getCustomerProfile().getGrade())")
    MemberDetailResponse toDetailResponse(Member member);

    MemberDetailResponse toDetailResponse(MemberDetailResult result);

    @Mapping(target = "orders", source = "orders")
    @Mapping(target = "grade", expression = "java(member.getCustomerProfile().getGrade())")
    MemberDetailWithOrdersResponse toDetailWithOrdersResponse(Member member);
    MemberSummaryResponse toSummaryResponse(Member member);

    MemberSummaryResponse toSummaryResponse(MemberSummaryResult result);


    //== List 변환 ==//
    List<MemberSummaryResponse> toListResponseList(List<Member> members);
}
