package com.mercury.star_be.global.error.code;

import org.springframework.http.HttpStatus;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum AuthenticationErrorCode  implements ErrorCode {

    // 400 Unauthorized - 잘못된 요청
    FAIL_DECRYPTION(HttpStatus.BAD_REQUEST, "Access 토큰 복호화 도중 오류가 발생하였습니다."),
    NOTEXIST_EXPIRATIONDATE_ACCESSTOKEN(HttpStatus.BAD_REQUEST, "Access 토큰에 만료기한이 존재하지 않습니다."),
    NOTEXIST_ID_ACCESSTOKEN(HttpStatus.BAD_REQUEST, "Access 토큰에 아이디가 존재하지 않습니다."),
    NOTCONVERTLONG_ID_ACCESSTOKEN(HttpStatus.BAD_REQUEST, "Access 토큰에 아이디 Long 타입으로 변환할 수 없습니다."),


    // 401 Unauthorized - 인증 실패
    USER_DEACTIVATED(HttpStatus.UNAUTHORIZED, "탈퇴한 유저입니다."),
    MISSING_ACCESSTOKEN(HttpStatus.UNAUTHORIZED, "Access 토큰이 없습니다."),
    MISSING_REFRESGTOKEN(HttpStatus.UNAUTHORIZED, "Access 토큰이 없습니다."),
    CREATE_AUTHENTICATION(HttpStatus.UNAUTHORIZED, "인증 객체 생성 중 오류가 발생하였습니다."),


    // 403 Forbidden - 인증 자격 증명은 있지만, 접근 권한이 불충분한 경우
    NO_AUTHENTICATION_CONTEXT(HttpStatus.FORBIDDEN, "인증 정보가 존재하지 않습니다."),

    // 500 Internal Server Error - 외부 서비스 문제로 토큰 발급 불가
    OAUTH2_PROVIDER_TOKEN(HttpStatus.INTERNAL_SERVER_ERROR, "Oauth2 프로바이더의 토큰을 발급 받을 수 없습니다.");

    private final HttpStatus httpStatus;
    private final String message;

}
