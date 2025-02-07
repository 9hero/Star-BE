package com.mercury.star_be.global.error.code;

import org.springframework.http.HttpStatus;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum CommonErrorCode implements ErrorCode {

	// 400 Bad Request
	VALIDATION_FAILED(HttpStatus.BAD_REQUEST, "입력이 잘못되었습니다."),

	// 500 Internal Server Error
	INTERNAL_SERVER_ERROR_DB(HttpStatus.INTERNAL_SERVER_ERROR, "데이터베이스 에러입니다."),
	INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서버 에러입니다.");

	private final HttpStatus httpStatus;
	private final String message;
}
