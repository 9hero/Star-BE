package com.mercury.star_be.global.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig{
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.csrf(auth -> auth.disable())       // CSRF 방어 기능 비활성화. front와의 연결은 WebConfig에 설정
                .headers(x -> x.frameOptions(y -> y.disable()))     // H2-console
                .authorizeHttpRequests(requests -> requests
                        .requestMatchers(
                                "/api/**" // front단에서의 요청
                        ).permitAll().requestMatchers("/**").permitAll() //기본 permiAll로 셋팅. 추후 변경 필요
                        .anyRequest().authenticated()  // 위 경로 말고 다른 경로들은 전부 인증필요
                );
        return http.build();
    }
}
