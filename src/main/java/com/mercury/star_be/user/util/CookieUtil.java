package com.mercury.star_be.user.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

@Component
public class CookieUtil {

    public final static int ACCESS_COOKIE_EXPIRATION = 60 * 10;
    public final static int REFRESH_COOKIE_EXPIRATION = 60 * 60 * 24;

    @Value("${host}")
    private static String host;

    public static ResponseCookie createCookie(String key, String value, Integer expiredS) {

        ResponseCookie cookie = ResponseCookie.from(key, value)
                .domain(".mercurystudy.store")  // local인 경우 제거
                .secure(true)  // HTTPS 연결에서만 전송
                .path("/")
                .maxAge(expiredS) 
                .sameSite("None") // 반드시 "None" 설정 필요
            .build();

        return cookie;
    }
}
