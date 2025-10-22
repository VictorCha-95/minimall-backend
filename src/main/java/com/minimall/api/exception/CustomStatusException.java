package com.minimall.api.exception;

import com.minimall.api.common.CustomStatus;
import com.minimall.api.domain.DomainType;

public class CustomStatusException extends RuntimeException {
    public CustomStatusException(String message) {
        super(message);
    }

  public CustomStatusException(DomainType domainType, Long id, CustomStatus currentStatus, CustomStatus targetStatus) {
    super(String.format("[%s: %d] 상태 오류  Current Status: %s, Try Status: %s," ,
            domainType, id, currentStatus, targetStatus));
  }
}
