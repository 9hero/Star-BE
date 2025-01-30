package com.mercury.star_be.global.error.code;

import org.springframework.http.HttpStatus;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum AuthenticationErrorCode  implements ErrorCode {

    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "인증 실패"),
    UNAUTHORIZED_TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED, "만료된 토큰"),
    UNAUTHORIZED_TOKEN_MISSING(HttpStatus.UNAUTHORIZED, "JWT 토큰이 없습니다."),
    UNAUTHORIZED_NO_AUTHENTICATION_CONTEXT(HttpStatus.UNAUTHORIZED, "인증 정보가 존재하지 않습니다."),
    UNAUTHORIZED_OAUTH2_PROVIDER_TOKEN(HttpStatus.UNAUTHORIZED, "Oauth2 프로바이더의 토큰을 발급 받을 수 없습니다.");
    private final HttpStatus httpStatus;
    private final String message;
}
