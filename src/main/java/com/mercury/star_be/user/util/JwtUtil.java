package com.mercury.star_be.user.util;

import com.mercury.star_be.global.error.CustomAuthenticationException;
import com.mercury.star_be.global.error.code.AuthenticationErrorCode;
import com.mercury.star_be.user.dto.response.UserResponse;
import com.mercury.star_be.user.entity.User;
import com.mercury.star_be.user.repository.UserRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;

@Component
public class JwtUtil {

    public final static Long ACCESS_TOKEN_EXPIRATION = 1000 * 60 * 60 * 1L; // 24시간
    public final static Long REFRESH_TOKEN_EXPIRATION = 1000 * 60 * 60 * 24L; // 24시간
    private final RedisTemplate<String, Object> redisTemplate;
    private final UserRepository userRepository;
    private final SecretKey secretKey;

    public JwtUtil(RedisTemplate<String, Object> redisTemplate, UserRepository userRepository, @Value("${spring.jwt.secret}") String secretValue) {
        this.redisTemplate = redisTemplate;
        this.userRepository = userRepository;
        this.secretKey = Keys.hmacShaKeyFor(secretValue.getBytes(StandardCharsets.UTF_8));
    }

    public static UserResponse getAuthenticatedUser(Authentication auth) {
        return auth != null && auth.isAuthenticated() && auth.getPrincipal() instanceof UserResponse ?
                (UserResponse) auth.getPrincipal() : null;
    }

    public Long getId(String token) {
        Claims claims = null;
        try {
            // 토큰 파싱 및 서명 검증 (parseClaimsJws 메소드 사용 권장)
            claims = Jwts.parser()
                    .setSigningKey(secretKey)
                    .build().parseSignedClaims(token).getPayload();
        } catch (ExpiredJwtException e) {
            // 만료된 토큰의 경우에도 claims가 존재하면, 해당 ID를 사용합니다.
            String idStr = e.getClaims().get("id").toString();
            if (idStr == null) {
                throw new CustomAuthenticationException(AuthenticationErrorCode.NOTEXIST_ID_ACCESSTOKEN);
            }
            try {
                return Long.valueOf(idStr);
            } catch (NumberFormatException ex) {
                throw new CustomAuthenticationException(AuthenticationErrorCode.NOTCONVERTLONG_ID_ACCESSTOKEN);
            }
        } catch (Exception e) {
            throw new CustomAuthenticationException(AuthenticationErrorCode.FAIL_DECRYPTION);
        }
        String idStr = claims.get("id").toString();
        if (idStr == null) {
            throw new CustomAuthenticationException(AuthenticationErrorCode.NOTEXIST_ID_ACCESSTOKEN);
        }
        try {
            return Long.valueOf(idStr);
        } catch (NumberFormatException ex) {
            throw new CustomAuthenticationException(AuthenticationErrorCode.NOTCONVERTLONG_ID_ACCESSTOKEN);
        }
    }




    public String getCategory(String token) {

        return Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token).getPayload().get("category", String.class);
    }

    public Date getExpiration(String token) {

        return Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token).getPayload().getExpiration();
    }

    public boolean isExpired(String token) {
        try {
            Claims claims = Jwts.parser()
                    .setSigningKey(secretKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            Date expiration = claims.getExpiration();
            // 만료 시간이 null이 아니고, 현재 시간보다 미래인 경우
            if (expiration != null && expiration.after(new Date())) {
                return false; // 아직 만료되지 않음
            }
            return true; // 만료됨
        } catch (ExpiredJwtException e) {
            return true; // 이미 만료된 토큰
        } catch (Exception e) { // 그 외 토큰 복호화 도중 실패 시
            throw new CustomAuthenticationException(AuthenticationErrorCode.FAIL_DECRYPTION);
        }
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
        Long id = getId(token);
        // UserRepository에서 사용자 조회 (Optional을 처리)
        User user = userRepository.findById(id).orElseThrow();
        UserResponse userResponseDto = new UserResponse(user);
        // Spring Security 인증 토큰 생성
        Authentication authToken = new UsernamePasswordAuthenticationToken(userResponseDto, null, userResponseDto.getAuthorities());
        // SecurityContext에 인증 정보 설정
        SecurityContextHolder.getContext().setAuthentication(authToken);
    }

    // 만료 시간을 문자열로 변환하는 메서드
    public String formatDateToString(Date expirationDate) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return sdf.format(expirationDate);
    }

    // 블랙리스트 토큰 값 반환 (문자열을 Date로 변환)
    public Date getBlacklistValue(Long tokenId) {
        // Redis에서 "BLACKLIST_" + tokenId에 해당하는 값을 가져옴 (문자열 형태)
        String blacklistDateString = (String) redisTemplate.opsForValue().get("BLACKLIST_" + tokenId);

        if (blacklistDateString != null) {
            try {
                // 가져온 문자열을 Date로 변환
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                return sdf.parse(blacklistDateString);  // 문자열을 Date로 파싱
            } catch (ParseException e) {
                e.printStackTrace();
                return null;  // 예외 발생 시 null 반환
            }
        }
        return null;  // Redis에서 값이 없으면 null 반환
    }


    public void addToBlacklist(Long tokenId, Date expirationDate, Long accessTokenExpiration) {
        // 만료 시간을 문자열로 변환
        String expirationTimeStr = formatDateToString(expirationDate);
        redisTemplate.opsForValue().set("BLACKLIST_" + tokenId, expirationTimeStr, accessTokenExpiration, TimeUnit.MILLISECONDS);
    }

    // 블랙리스트에서 토큰을 제거
    public void removeFromBlacklist(Long tokenId) {
        redisTemplate.delete("BLACKLIST_" + tokenId);
    }
}

