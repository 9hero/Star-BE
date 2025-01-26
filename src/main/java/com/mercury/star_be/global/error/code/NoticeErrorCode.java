package com.mercury.star_be.global.error.code;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum NoticeErrorCode implements ErrorCode{

    NOTICE_NOT_FOUND(HttpStatus.NOT_FOUND, "공지사항이 존재하지 않습니다."),
    NOTICE_NOT_IN_GROUP(HttpStatus.NOT_FOUND, "공지사항이 그룹 내에 없습니다.");
    private final HttpStatus httpStatus;
    private final String message;
}
