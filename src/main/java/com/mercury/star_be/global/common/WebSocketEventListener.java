package com.mercury.star_be.global.common;

import com.mercury.star_be.timer.dto.TimerDto;
import com.mercury.star_be.timer.dto.TimerEvent;
import com.mercury.star_be.timer.service.TimerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SetOperations;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.time.Duration;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Component
@RequiredArgsConstructor
public class WebSocketEventListener {

    private final AtomicInteger activeUsers = new AtomicInteger(0);
    private final RedisTemplate<String, String> redisTemplate;
    private final TimerService timerService;
    private final SimpMessagingTemplate messagingTemplate;

    @EventListener
    public void handleWebSocketConnectListener(SessionConnectEvent event) {
        // Entry 체크
        activeUsers.incrementAndGet();
        System.out.println("사용자 연결됨. 현재 연결된 사용자 수: " + activeUsers.get());

        // header 체크
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());

        // FocusRoom CONNECT 이벤트인 경우
        String roomType = headerAccessor.getFirstNativeHeader("roomType");
        if (roomType != null && roomType.equals("focus")){
            // FocusRoom CONNECT 이벤트 핸들러
            // 접속한 사용자의 정보를 Redis에 저장, 브로드캐스트
            handleFocusRoomConnection(headerAccessor);
        }
    }

    @EventListener
    // 세션 종료 이벤트 (클라이언트가 종료 요청을 보내지 못한 경우 예외처리)
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
        activeUsers.decrementAndGet();
        System.out.println("사용자 연결 해제됨. 현재 연결된 사용자 수: " + activeUsers.get());
        System.out.println("종료 세션 id "+event.getSessionId());
        System.out.println("세션 속성들 조회 "+event);
        handleTimerDisconnection(event);
    }

    private void handleTimerDisconnection(SessionDisconnectEvent event) {
        // Redis에서 sessionId로 groupId와 userId 조회
        SetOperations<String, String> setOps = redisTemplate.opsForSet();
        Set<String> groupIdAndUserId = setOps.members(event.getSessionId());

        // Redis에 세션 존재 시, 집중방 유저 접속 중임
        if (groupIdAndUserId != null && !groupIdAndUserId.isEmpty()){
            String[] split = groupIdAndUserId.iterator().next().split(":");
            long groupId = Long.parseLong(split[0]);
            long userId = Long.parseLong(split[1]);
            String nickname = split[2];

            System.out.println("groupId: " + groupId + " userId: " + userId);

            // 타이머 정지 처리
            System.out.println("sessionId: " + event.getSessionId());
            timerService.stopTimerByGroupIdAndUserId(groupId, userId);

            // Redis에서 삭제
            redisTemplate.delete(event.getSessionId()); // 세션id 제거
            Long removeCUser = redisTemplate.opsForSet().remove("focus:" + groupId, userId + ":" + nickname);
            System.out.println("현재 유저 제거 완료 : " + removeCUser);

            // Disconnection 브로드캐스트
            TimerDto disconnectEvent = new TimerDto();
            disconnectEvent.setUserId(userId);
            disconnectEvent.setEvent(TimerEvent.DISCONNECT);
            messagingTemplate.convertAndSend(
                    "/sub/groups/"+groupId+"/timers",
                    disconnectEvent
            );
        }
    }

    private void handleFocusRoomConnection(StompHeaderAccessor headerAccessor) {
        // 헤더에서 groupId와 userId 추출 닉네임도 추출 -db or client에서 가져옴
        String groupId = headerAccessor.getFirstNativeHeader("groupId");
        String userId = headerAccessor.getFirstNativeHeader("userId");
        String nickname = headerAccessor.getFirstNativeHeader("nickname");

        System.out.println("Checking headers: groupId " + groupId + " uid: " + userId + " nick: " + nickname);

        // focus 방일 경우만 처리 - focus 방은 참여 인원 Redis에 저장
        // disconnect 용 세션id 필요. 하지만 sid로 접속 유저 체크 불가.
        if (groupId != null && userId != null) {
            String redisKey = "focus:" + groupId;
            SetOperations<String, String> setOps = redisTemplate.opsForSet();

            // 세션 id 저장 (세션 종료 시, Redis에서 제거하고 timer도 종료 : groupId, userId로 조회)
            String sessionId = headerAccessor.getSessionId();
            if (sessionId != null) {
                setOps.add(sessionId, groupId+":"+userId+":"+nickname);
            }else {
                // 세션 아이디가 없을 경우 예외 처리
                log.error("SessionId is null");
                throw new RuntimeException("SessionId is null");
            }

            // Redis에 유저 추가
            setOps.add(redisKey, userId+":"+nickname);

            // TTL 설정 (테스트 용 180초)
//            redisTemplate.expire(redisKey, Duration.ofSeconds(180));

            System.out.println("User " + userId + " joined group " + groupId);

            // Entry 이벤트 브로드캐스트 객체
            TimerDto entryEvent = new TimerDto();
            entryEvent.setEvent(TimerEvent.ENTRY);
            entryEvent.setUserId(Long.parseLong(userId));
            entryEvent.setNickname(nickname);
            entryEvent.setStatus("rest");

            // Entry 이벤트 브로드캐스트
            messagingTemplate.convertAndSend("/sub/groups/"+groupId+"/timers", entryEvent);
        }
    }
}
