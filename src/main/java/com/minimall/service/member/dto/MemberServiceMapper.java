package com.minimall.service.member.dto;

import com.minimall.api.member.dto.response.MemberDetailWithOrdersResponse;
import com.minimall.api.member.dto.response.MemberSummaryResponse;
import com.minimall.api.order.dto.OrderApiMapper;
import com.minimall.domain.embeddable.Address;
import com.minimall.domain.member.Member;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(
        componentModel = "spring",
        uses = OrderApiMapper.class,
        unmappedTargetPolicy = ReportingPolicy.ERROR
)
public interface MemberServiceMapper {

    // == Command -> Domain == //
    default Member toEntity(MemberCreateCommand command) {

        Address address = null;
        if (command.addr() != null) {
                address = new Address(
                    command.addr().postcode(),
                    command.addr().state(),
                    command.addr().city(),
                    command.addr().street(),
                    command.addr().detail()
            );
        }

        return Member.registerCustomer(
                command.loginId(),
                command.password(),
                command.name(),
                command.email(),
                address
        );
    }

    // == Domain -> Response == //
    MemberSummaryResult toSummaryResult(Member member);

    List<MemberSummaryResponse> toSummaryResponseList(List<Member> members);

    @Mapping(target = "grade", expression = "java(member.getCustomerProfile().getGrade())")
    MemberDetailResult toDetailResult(Member member);

    @Mapping(target = "orders", source = "orders")
    @Mapping(target = "grade", expression = "java(member.getCustomerProfile().getGrade())")
    MemberDetailWithOrdersResponse toDetailWithOrdersResponse(Member member);
}
