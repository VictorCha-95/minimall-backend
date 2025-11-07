package com.minimall.domain.exception;

import com.minimall.domain.common.CustomStatus;
import com.minimall.domain.common.DomainType;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
public class DomainStatusException extends DomainRuleException {

    private final DomainType domain;
    private final Long id;
    private final CustomStatus currentStatus;
    private final CustomStatus targetStatus;


    public DomainStatusException(DomainType domain, Long id, CustomStatus currentStatus, CustomStatus targetStatus) {
        super(DomainExceptionMessage.STATUS_ERROR.text(domain.name(), id, currentStatus, targetStatus));
        this.domain = domain;
        this.id = id;
        this.currentStatus = currentStatus;
        this.targetStatus = targetStatus;
    }
}
