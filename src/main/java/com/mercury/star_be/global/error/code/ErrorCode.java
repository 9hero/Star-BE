package com.mercury.star_be.global.error.code;

import org.springframework.http.HttpStatus;

public interface ErrorCode {

	HttpStatus getHttpStatus();

	String getMessage();
}
