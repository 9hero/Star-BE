package com.mercury.star_be.user.util;

import com.mercury.star_be.user.util.JwtUtil;
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
            "/oauth2-jwt-header"
    ));

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String requestPath = request.getServletPath();
        if (requestPath.startsWith("/fileupload") || requestPath.startsWith("/chat") || requestPath.startsWith("/timer")) return true;
        return excludeUrls.contains(requestPath);
    }
    private void sendUnauthorized(HttpServletResponse res, String message) throws IOException {
        res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        res.getWriter().write(message);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain filterChain) throws ServletException, IOException {
        long startTime = System.currentTimeMillis(); // 요청 시작 시간 기록

        // URL 확인
        String fullUrl = req.getRequestURL().toString();

        // 토큰 체크
        String accessToken = jwtUtil.getJwt(req);
        if (accessToken == null) {
            sendUnauthorized(res, "Access token is missing or invalid");
            return;
        }

        // 토큰 Category 체크
        if (!jwtUtil.isCheckCategoryToken(accessToken)) {
            sendUnauthorized(res, "Invalid access token");
            return;
        }

        // 토큰 유효시간 체크
        if (jwtUtil.isExpired(accessToken)) {
            sendUnauthorized(res, "Access token time is expired");
            return;
        }

        // 토큰 이용하여 시큐리티 내 인증객체 생성
        try {
            jwtUtil.createAuthentication(accessToken);
        } catch (Exception e) {
            sendUnauthorized(res, "Authentication creation failed");
            return;
        }

        System.out.println("검증 완료");
        // 다음 필터로 요청 전달
        filterChain.doFilter(req, res);
    }

    // 실행 시간 로그 출력
    private void logExecutionTime(long startTime) {
        long endTime = System.currentTimeMillis(); // 끝 시간
        System.out.println("Request processing time: " + (endTime - startTime) + "ms");
    }
}
