package com.mercury.star_be.user.util;

import com.mercury.star_be.user.dto.response.UserResponse;
import com.mercury.star_be.user.entity.User;
import com.mercury.star_be.user.repository.UserRepository;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class JwtUtil {

    public final static Long ACCESS_TOKEN_EXPIRATION = 1000 * 60 * 60 * 24L; // 24시간
    public final static Long REFRESH_TOKEN_EXPIRATION = 1000 * 60 * 60 * 24L; // 24시간
    private final UserRepository userRepository;
    private final SecretKey secretKey;

    public JwtUtil(UserRepository userRepository, @Value("${spring.jwt.secret}") String secretValue) {
        this.userRepository = userRepository;
        this.secretKey = Keys.hmacShaKeyFor(secretValue.getBytes(StandardCharsets.UTF_8));
    }


    public Long getid(String token) {

        return Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token).getPayload().get("id", Long.class);
    }

    public String getNickname(String token) {

        return Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token).getPayload().get("oauthId", String.class);
    }

    public String getCategory(String token) {

        return Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token).getPayload().get("category", String.class);
    }

    public Boolean isExpired(String token) {

        return Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token).getPayload().getExpiration().before(new Date());
    }

    public boolean isCheckCategoryToken(String token) {
        return getCategory(token).equals("access");
    }

    public String getJwt(HttpServletRequest req) {
        String authorizationHeader = req.getHeader("Authorization");
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            return authorizationHeader.substring(7);
        }
        return null; // 토큰이 없는 경우 null 반환
    }





    public String createJwt(String category, Long id, Long expiredMs) {
        return Jwts.builder()
                .claim("category", category)
                .claim("id", id)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + expiredMs))
                .signWith(secretKey)
                .compact();
    }

    public void createAuthentication(String token) {
        Long id = getid(token);
        // UserRepository에서 사용자 조회 (Optional을 처리)
        User user = userRepository.findById(id).orElseThrow();
        UserResponse userResponseDto = new UserResponse(user);
        // Spring Security 인증 토큰 생성
        Authentication authToken = new UsernamePasswordAuthenticationToken(userResponseDto, null, userResponseDto.getAuthorities());
        // SecurityContext에 인증 정보 설정
        SecurityContextHolder.getContext().setAuthentication(authToken);
    }

    public UserResponse getAuthenticatedUser(Authentication auth) {
        return auth != null && auth.isAuthenticated() && auth.getPrincipal() instanceof UserResponse ?
                (UserResponse) auth.getPrincipal() : null;
    }

}

