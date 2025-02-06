package com.mercury.star_be.user.Handler;

import com.mercury.star_be.global.error.CustomAuthenticationException;
import com.mercury.star_be.global.error.code.AuthenticationErrorCode;
import com.mercury.star_be.user.entity.RefreshToken;
import com.mercury.star_be.user.entity.User;
import com.mercury.star_be.user.repository.RefreshRepository;
import com.mercury.star_be.user.util.CookieUtil;
import com.mercury.star_be.user.util.JwtUtil;
import jakarta.persistence.EntityManager;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Date;

@Component
@RequiredArgsConstructor
public class CustomSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtUtil jwtUtil;
    private final RefreshRepository refreshRepository;
    private final EntityManager entityManager;


    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {

        // OAuth2User
        // UserResponse UserResponseDto = (UserResponse) authentication.getPrincipal();
        // User user = (User) authentication.getPrincipal();
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Long id  = user.getId();
        // User 객체가 영속성 컨텍스트에 있으면 해당 객체를 가져옵니다.
        User persistentUser = entityManager.find(User.class, user.getId());

        // 토큰 생성
        String accessToken = jwtUtil.createJwt("access", id, jwtUtil.ACCESS_TOKEN_EXPIRATION);    // 24시간
        // String refreshToken = jwtUtil.createJwt("refresh", id, jwtUtil.REFRESH_TOKEN_EXPIRATION); // 24시간


        /** 현재 Redis 설정 안됌
        //  Redis에 access 토큰 정보 확인 및 블랙리스트 등록
        jwtUtil.addToBlacklist(jwtUtil.getId(accessToken), jwtUtil.getExpiration(accessToken), jwtUtil.ACCESS_TOKEN_EXPIRATION);
        **/

        // RefreshToken 조회
//        RefreshToken existingToken = refreshRepository.findByUser_Id(persistentUser.getId())
//                .orElseThrow(() -> new CustomAuthenticationException(AuthenticationErrorCode.MISSING_REFRESGTOKEN));

        RefreshToken existingToken = refreshRepository.findByUser_Id(persistentUser.getId())
                .orElse(null);

        Date expirationDate = new Date(System.currentTimeMillis()+jwtUtil.REFRESH_TOKEN_EXPIRATION);
        Date createdDate = new Date(System.currentTimeMillis());
        if (existingToken != null) {
            // 기존 토큰 업데이트
            existingToken.setExpiredAt(expirationDate);
            existingToken.setCreatedAt(createdDate);
        } else {
            // 새로운 토큰 생성
            existingToken = RefreshToken.builder()
                    .user(persistentUser)
                    .expiredAt(expirationDate)
                    .build();
        }
        refreshRepository.save(existingToken);


        // 응답 설정
        response.addHeader(HttpHeaders.SET_COOKIE, CookieUtil.createCookie("access", accessToken, CookieUtil.ACCESS_COOKIE_EXPIRATION).toString());
        // response.addHeader(HttpHeaders.SET_COOKIE, CookieUtil.createCookie("refresh", refreshToken, CookieUtil.REFRESH_COOKIE_EXPIRATION).toString());

        //원래 요청된 URL로 리다이렉트
        String redirectUrl = (String) request.getSession().getAttribute("SPRING_SECURITY_SAVED_REQUEST_URL");
        if (redirectUrl == null) {
            redirectUrl = "http://localhost:5173/oauth2/callback";
        }

         response.sendRedirect(redirectUrl); // 리다이렉트 URL로 이동
    }
}