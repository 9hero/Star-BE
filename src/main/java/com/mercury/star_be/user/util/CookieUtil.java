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

        ResponseCookie.ResponseCookieBuilder cookieBuilder = ResponseCookie.from(key, value)
                .secure(true)  // HTTPS 연결에서만 전송
                .path("/")
                .maxAge(expiredS) 
                .sameSite("None"); // 반드시 "None" 설정 필요

        if (host.endsWith(".store")) {
            cookieBuilder.domain(".mercurystudy.store");
        }

        return  cookieBuilder.build();

//        Cookie cookie = new Cookie(key, value);
//        cookie.setMaxAge(expiredS);
//        cookie.setPath("/"); // 쿠키가 유효한 경로 설정
//        // cookie.setDomain("http://localhost:5173");
//        return cookie;
    }
}
