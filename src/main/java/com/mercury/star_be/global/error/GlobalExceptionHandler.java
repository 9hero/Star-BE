package com.mercury.star_be.global.error;

import org.hibernate.exception.ConstraintViolationException;
import org.springframework.dao.DataAccessException;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.mercury.star_be.global.error.code.ErrorCode;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

	// 비즈니스 로직 에러
	@ExceptionHandler(BusinessException.class)
	protected ResponseEntity<ErrorResponse> handleBusinessException(BusinessException ex) {
		ErrorCode errorCode = ex.getErrorCode();
		log.error("BusinessException: ", ex);
		return ResponseEntity.status(errorCode.getHttpStatus())
			.body(new ErrorResponse(errorCode.getHttpStatus().toString(), errorCode.getMessage()));
	}

	// 500 INTERNAL SERVER ERROR
	// 데이터 베이스 에러
	@ExceptionHandler(DataAccessException.class)
	protected ResponseEntity<ErrorResponse> handleDataAccessException(DataAccessException ex) {
		ErrorCode errorCode = ErrorCode.INTERNAL_SERVER_ERROR_DB;
		log.error("DataAccessException: ", ex);
		return ResponseEntity.status(errorCode.getHttpStatus())
			.body(new ErrorResponse(errorCode.getHttpStatus().toString(), errorCode.getMessage()));
	}

	// 400 BAD REQUEST
	// @Valid 검증 실패
	@ExceptionHandler({MethodArgumentNotValidException.class, ConstraintViolationException.class,
		MissingServletRequestParameterException.class, BindException.class})
	protected ResponseEntity<ErrorResponse> handleValidateException(Exception ex) {
		ErrorCode errorCode = ErrorCode.VALIDATION_FAILED;
		log.error("ValidException: ", ex);
		return ResponseEntity.status(errorCode.getHttpStatus())
			.body(new ErrorResponse(errorCode.getHttpStatus().toString(), errorCode.getMessage()));
	}

	// 나머지 에러
	@ExceptionHandler(Exception.class)
	protected ResponseEntity<ErrorResponse> handleException(Exception ex) {
		ErrorCode errorCode = ErrorCode.INTERNAL_SERVER_ERROR;
		log.error("Exception: ", ex);
		return ResponseEntity.status(errorCode.getHttpStatus())
			.body(new ErrorResponse(errorCode.getHttpStatus().toString(), errorCode.getMessage()));
	}
}
