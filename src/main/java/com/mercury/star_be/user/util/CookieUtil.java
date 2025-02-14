package com.mercury.star_be.user.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

import jakarta.servlet.http.HttpServletRequest;

@Component
public class CookieUtil {

    public final static int ACCESS_COOKIE_EXPIRATION = 60 * 10;
    public final static int REFRESH_COOKIE_EXPIRATION = 60 * 60 * 24;

    public static ResponseCookie createCookie(String key, String value, Integer expiredS, HttpServletRequest request) {

        ResponseCookie.ResponseCookieBuilder cookieBuilder = ResponseCookie.from(key, value)
                .secure(true)  // HTTPS 연결에서만 전송
                .path("/")
                .maxAge(expiredS) 
                .sameSite("None"); // 반드시 "None" 설정 필요

        String host = request.getServerName(); // ex) back.mercurystudy.store, localhost

        if (host.endsWith(".store")) {
            cookieBuilder.domain(".mercurystudy.store");
        }

        return cookieBuilder.build();
    }
}
