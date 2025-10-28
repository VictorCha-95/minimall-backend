package com.minimall.exception;

import com.minimall.domain.common.CustomStatus;
import com.minimall.domain.DomainType;

public class DomainStatusException extends RuntimeException {
    public DomainStatusException(String message) {
        super(message);
    }

  public DomainStatusException(DomainType domain, Long id, CustomStatus currentStatus, CustomStatus targetStatus) {
    super(String.format("[%s - id: %d] 상태 오류  Current Status: %s, Try Status: %s," ,
            domain.name(), id, currentStatus, targetStatus));
  }
}
