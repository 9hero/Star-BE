package com.mercury.star_be.global.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
public class WebSocketBrokerConfiguration implements WebSocketMessageBrokerConfigurer {

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // 웹소켓 연결 요청 엔드포인트
        // ws://localhost:8080/chat/
        registry.addEndpoint("/chat")
                .setAllowedOrigins("http://localhost:5173");

        // 웹소켓 연결 요청 엔드포인트
        // ws://localhost:8080/timer/
        registry.addEndpoint("/timer")
                .setAllowedOrigins("http://localhost:5173");

    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // 구독 요청을 처리할 prefix 설정
        registry.enableSimpleBroker("/sub");

        // 클라이언트에서 메시지를 보낼 때 사용할 prefix 설정
        registry.setApplicationDestinationPrefixes("/pub");
    }

}
