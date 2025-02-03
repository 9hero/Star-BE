package com.mercury.star_be.global.error.code;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum UserErrorCode implements ErrorCode {

    USER_NOT_EXIST(HttpStatus.CONFLICT, "존재하지 않는 유져입니다"),
    ALREADY_BLOCKED_USER(HttpStatus.CONFLICT, "이미 차단한 유져입니다."),
    NOT_BLOCKED_USER(HttpStatus.CONFLICT, "차단하지 않은 유져입니다.");

    private final HttpStatus httpStatus;

    private final String message;
}
