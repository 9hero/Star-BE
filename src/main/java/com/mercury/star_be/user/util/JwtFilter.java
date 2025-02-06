package com.mercury.star_be.user.util;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter { //각 요청에 대해 딱 한 번만 필터를 실행

    private final JwtUtil jwtUtil;
    private final Set<String> excludeUrls = new HashSet<>(Set.of(
            "/", "/login", "/oauth2Login",
            "/favicon.ico", // 오타 수정
            "/oauth2/callback",
            "/oauth2-jwt-header",
            "/error" , // 에러 컨트롤러에 대한 포워딩 요청
            "/api/auth/reissue" // 토큰 재발급
    ));

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String requestPath = request.getServletPath();
        if (requestPath.matches("^/api/groups/\\d+$") || requestPath.matches("^/api/groups/\\d+/timers$")) return false;
        if (requestPath.startsWith("/fileupload") ||
                requestPath.startsWith("/timer") ||
                requestPath.startsWith("/chat") ||
                requestPath.startsWith("/api/groups")) return true;
        return excludeUrls.contains(requestPath);
    }

    private void sendUnauthorized(HttpServletResponse res, String message) throws IOException {
        res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        res.getWriter().write(message);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain filterChain) throws ServletException, IOException {

        // 토큰 체크
        String accessToken = jwtUtil.getJwt(req);
        if (accessToken == null) {
             sendUnauthorized(res, "Access token is missing");
             return;
        }

        // 토큰 유효시간 체크
        boolean test = jwtUtil.isExpired(accessToken);
        if (test) {
            sendUnauthorized(res, "Access token time is expired");
            return;
        }

        // 토큰 Category 체크
        if (!jwtUtil.isCheckCategoryToken(accessToken)) {
            sendUnauthorized(res, "Token Category is not access token");
            return;
        }

        /** 현재 Redis 설정 안됌
        // 블랙리스트 검증
        if (jwtUtil.getExpiration(accessToken).before(jwtUtil.getBlacklistValue(jwtUtil.getId(accessToken)))) {
            sendUnauthorized(res, "Access token is blacklisted");
            return;
        }
        **/

        // 토큰 이용하여 시큐리티 내 인증객체 생성
        jwtUtil.createAuthentication(accessToken);


        System.out.println("검증 완료");
        // 다음 필터로 요청 전달
        filterChain.doFilter(req, res);
    }
}

