package com.mercury.star_be.user.Handler;

import com.mercury.star_be.user.dto.response.UserResponse;
import com.mercury.star_be.user.util.CookieUtil;
import com.mercury.star_be.user.util.JwtUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class CustomSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtUtil jwtUtil;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {

        // OAuth2User
        UserResponse UserResponseDto = (UserResponse) authentication.getPrincipal();
        Long id  = UserResponseDto.getId();

        // 토큰 생성
        String accessToken = jwtUtil.createJwt("access", id, jwtUtil.ACCESS_TOKEN_EXPIRATION);    // 10분
        String refreshToken = jwtUtil.createJwt("refresh", id, jwtUtil.REFRESH_TOKEN_EXPIRATION); // 24시간

        // refresh 토큰 DB 저장
        // refreshTokenService.saveRefresh(username, expireS, refresh);

        // 응답 설정
        response.addHeader(HttpHeaders.SET_COOKIE, CookieUtil.createCookie("access", accessToken, CookieUtil.ACCESS_COOKIE_EXPIRATION).toString());
        response.addHeader(HttpHeaders.SET_COOKIE, CookieUtil.createCookie("refresh", refreshToken, CookieUtil.REFRESH_COOKIE_EXPIRATION).toString());
        response.addHeader("userid", id.toString());
        //원래 요청된 URL로 리다이렉트
        String redirectUrl = (String) request.getSession().getAttribute("SPRING_SECURITY_SAVED_REQUEST_URL");
        if (redirectUrl == null) {
            // 기본 리다이렉트 URL (로그인 후 이동할 URL)
        }
        redirectUrl = "http://localhost:5173/oauth2/callback";
        response.sendRedirect(redirectUrl); // 리다이렉트 URL로 이동
    }
}