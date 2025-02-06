package com.mercury.star_be.global.error;

import com.mercury.star_be.global.error.code.CommonErrorCode;
import com.mercury.star_be.global.error.code.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

	// 비즈니스 로직 에러
	@ExceptionHandler(BusinessException.class)
	protected ResponseEntity<ErrorResponse> handleBusinessException(BusinessException ex) {
		ErrorCode errorCode = ex.getErrorCode();
		log.error("BusinessException: {}", ex.getMessage());
		return ResponseEntity.status(errorCode.getHttpStatus())
			.body(new ErrorResponse(errorCode.getHttpStatus().toString(), errorCode.getMessage()));
	}

	// 500 INTERNAL SERVER ERROR
	// 데이터 베이스 에러
	@ExceptionHandler(DataAccessException.class)
	protected ResponseEntity<ErrorResponse> handleDataAccessException(DataAccessException ex) {
		ErrorCode errorCode = CommonErrorCode.INTERNAL_SERVER_ERROR_DB;
		log.error("DataAccessException: {}", ex.getMessage());
		return ResponseEntity.status(errorCode.getHttpStatus())
			.body(new ErrorResponse(errorCode.getHttpStatus().toString(), errorCode.getMessage()));
	}

	// 400 BAD REQUEST
	// @Valid 검증 실패
	@ExceptionHandler(MethodArgumentNotValidException.class)
	protected ResponseEntity<ErrorResponse> handleValidateException(MethodArgumentNotValidException ex) {
		ErrorCode errorCode = CommonErrorCode.VALIDATION_FAILED;
		ex.getBindingResult().getAllErrors().forEach(error -> {
			String fieldName = ((FieldError) error).getField();
			String errorMessage = error.getDefaultMessage();
			log.error("ValidException: {} : {}", fieldName, errorMessage);
		});
		return ResponseEntity.status(errorCode.getHttpStatus())
			.body(new ErrorResponse(errorCode.getHttpStatus().toString(), errorCode.getMessage()));
	}


	// 401 Unauthorized - 인증 실패
	@ExceptionHandler(CustomAuthenticationException.class)
	public ResponseEntity<ErrorResponse> handleAuthenticationAuthException(CustomAuthenticationException ex) {
		ErrorCode errorCode = ex.getErrorCode();
		log.error("BusinessException: {}", ex.getMessage());
		return ResponseEntity.status(errorCode.getHttpStatus())
				.body(new ErrorResponse(errorCode.getHttpStatus().toString(), errorCode.getMessage()));
	}





	// 나머지 에러
	@ExceptionHandler(Exception.class)
	protected ResponseEntity<ErrorResponse> handleException(Exception ex) {
		ErrorCode errorCode = CommonErrorCode.INTERNAL_SERVER_ERROR;
		log.error("Exception: {}", ex.getMessage());
		return ResponseEntity.status(errorCode.getHttpStatus())
			.body(new ErrorResponse(errorCode.getHttpStatus().toString(), errorCode.getMessage()));
	}
}
