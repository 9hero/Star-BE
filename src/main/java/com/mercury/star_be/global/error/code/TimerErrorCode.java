package com.mercury.star_be.global.error.code;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum TimerErrorCode implements ErrorCode{

    // 타이머 조회 실패
    TIMER_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 타이머입니다.");

    private final HttpStatus httpStatus;
    private final String message;

}
