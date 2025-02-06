package com.mercury.star_be.global.error;

import com.mercury.star_be.global.error.code.ErrorCode;
import lombok.Getter;
import org.springframework.security.core.AuthenticationException;

@Getter
public class CustomAuthenticationException extends AuthenticationException {

    private final ErrorCode errorCode;

    public CustomAuthenticationException(ErrorCode errorCode) {
        super(errorCode.getMessage()); // detailMessage 초기화
        this.errorCode = errorCode;
    }
    public ErrorCode getErrorCode() {
        return errorCode;
    }
}
