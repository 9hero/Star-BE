package com.mercury.star_be.global.common;

import com.mercury.star_be.timer.dto.event.FocusRoomConnectedEvent;
import com.mercury.star_be.timer.dto.event.FocusRoomDisconnectedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SetOperations;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.util.Set;

@Slf4j
@Component
@RequiredArgsConstructor
public class WebSocketEventListener {

    private final ApplicationEventPublisher eventPublisher;
    private final RedisTemplate<String, String> redisTemplate;

    /**
     * WebSocket 연결 시, 관련 이벤트 발행 함수.
     * header에 roomType을 명시하여 분기.
     * 집중방: focus,
     * 채팅방: chat
     * @param event
     */
    @EventListener
    public void handleWebSocketConnectListener(SessionConnectEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        String roomType = headerAccessor.getFirstNativeHeader("roomType");

        if (roomType != null && roomType.equals("focus")) {
            handleFocusRoomConnection(headerAccessor);
        }
    }

    private void handleFocusRoomConnection(StompHeaderAccessor headerAccessor) {
        String groupId = headerAccessor.getFirstNativeHeader("groupId");
        String groupMemberId = headerAccessor.getFirstNativeHeader("groupMemberId");
        String userId = headerAccessor.getFirstNativeHeader("userId");
        String nickname = headerAccessor.getFirstNativeHeader("nickname");
        String sessionId = headerAccessor.getSessionId();

        if (userId != null && groupMemberId != null && sessionId != null) {
            FocusRoomConnectedEvent connectedEvent = new FocusRoomConnectedEvent(
                    sessionId, groupId, groupMemberId, userId, nickname
            );
            eventPublisher.publishEvent(connectedEvent);
        }
    }

    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
        String sessionId = event.getSessionId();
        SetOperations<String, String> setOps = redisTemplate.opsForSet();
        Set<String> groupIdAndUserIdAndNickname = setOps.members("focus:" + sessionId);

        if (groupIdAndUserIdAndNickname != null && !groupIdAndUserIdAndNickname.isEmpty()) {
            String[] split = groupIdAndUserIdAndNickname.iterator().next().split(":");
            long groupId = Long.parseLong(split[0]);
            long userId = Long.parseLong(split[1]);
            long groupMemberId = Long.parseLong(split[2]);
            String nickname = split[3];

            FocusRoomDisconnectedEvent disconnectedEvent = new FocusRoomDisconnectedEvent(
                    sessionId, groupId, userId, groupMemberId, nickname
            );
            eventPublisher.publishEvent(disconnectedEvent);
        }
    }

}
