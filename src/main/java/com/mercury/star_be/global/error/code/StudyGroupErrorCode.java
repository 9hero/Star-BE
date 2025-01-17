package com.mercury.star_be.global.error.code;

import org.springframework.http.HttpStatus;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum StudyGroupErrorCode implements ErrorCode {

	// 스터디 그룹 관련 에러
	STUDY_GROUP_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 스터디 그룹입니다.");

	private final HttpStatus httpStatus;
	private final String message;
}
