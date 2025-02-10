package com.mercury.star_be.global.common;

import com.mercury.star_be.studygroup.entity.ConnectionStatus;
import com.mercury.star_be.studygroup.service.StudyGroupSseService;
import com.mercury.star_be.timer.dto.TimerDto;
import com.mercury.star_be.timer.dto.TimerEvent;
import com.mercury.star_be.timer.service.TimerService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitMessagingTemplate;
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
    private final RabbitMessagingTemplate rabbitTemplate;
    private final StudyGroupSseService studyGroupSseService;

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

    /**
     * Timer DISCONNECT 이벤트 핸들러
     * @param event
     */
    private void handleTimerDisconnection(SessionDisconnectEvent event) {
        // Redis에서 sessionId로 groupId와 userId 조회
        String sessionId = event.getSessionId();
        SetOperations<String, String> setOps = redisTemplate.opsForSet();
        Set<String> groupIdAndUserIdAndNickname = setOps.members("focus:"+event.getSessionId());

        // Redis에 세션 존재 시, 집중방 유저 접속 중임
        if (groupIdAndUserIdAndNickname != null && !groupIdAndUserIdAndNickname.isEmpty()){
            String[] split = groupIdAndUserIdAndNickname.iterator().next().split(":");
            long groupId = Long.parseLong(split[0]);
            long userId = Long.parseLong(split[1]);
            String nickname = split[2];

            log.info("disconnect groupId: {} userId: {} nickname : {}", groupId, userId,nickname);

            // 타이머 정지 처리
            System.out.println("sessionId: " + event.getSessionId());
            timerService.stopTimerByGroupIdAndUserId(groupId, userId);

            // Redis에서 삭제
            Long delete = redisTemplate.opsForSet().remove("focus:"+sessionId,groupId+":"+userId+":"+nickname);// 세션id 제거
            Long removeCUser = redisTemplate.opsForSet().remove("focus:" + groupId, userId + ":" + nickname);
            log.info("Remove session(T/F) and user : {} and {}",delete,removeCUser);

            // Disconnection 브로드캐스트
            TimerDto disconnectEvent = new TimerDto();
            disconnectEvent.setUserId(userId);
            disconnectEvent.setEvent(TimerEvent.DISCONNECT);
            messagingTemplate.convertAndSend(
                    "/topic/groups."+groupId+".timers",
                    disconnectEvent
            );
            System.out.println("타이머 종료 이벤트 브로드캐스트 완료");

            // SSE: 집중방 Disconnect 시 현재 인원수 send
            int focusRoomMemberCount = redisTemplate.opsForSet().members("focus:" + groupId).size();
            studyGroupSseService.sendFocusRoomMemberCountToGroup(groupId, focusRoomMemberCount);
            // SSE: 집중방 Disconnect 시 접속중 상태 send
            studyGroupSseService.sendMemberStatusToGroup(groupId, userId, ConnectionStatus.ONLINE);
        }
    }

    /**
     * FocusRoom CONNECT 이벤트 핸들러
     * @param headerAccessor
     */
    private void handleFocusRoomConnection(StompHeaderAccessor headerAccessor) {
        // 헤더에서 groupId와 userId 추출 닉네임도 추출 -db or client에서 가져옴
        String groupId = headerAccessor.getFirstNativeHeader("groupId");
        String userId = headerAccessor.getFirstNativeHeader("userId");

        //TODO 그룹 멤버 닉네임으로 필요함...
        String nickname = headerAccessor.getFirstNativeHeader("nickname");
        log.info("chekc nickname: {}", nickname);
        /*
            // JWT 토큰을 이용한 사용자 정보 조회인데 현재 토큰 없이 connect 해서 오류 발생
            필터 적용은 가능하나 사용 불가 csrf 비활성 불가능함
            : EnableWebSecurity 설정으로 인해 필터 적용가능 csrf 비활성화 불가능
            UserResponse userResponse = UserResponse.getAuthenticatedUser();
            if (userResponse != null) {
                System.out.println("UserResponse: " + userResponse);
                userId = userResponse.getId().toString();
                nickname = userResponse.getNickname();
            }
         */

        System.out.println("Checking headers: groupId " + groupId + " uid: " + userId + " nick: " + nickname);

        // focus 방일 경우만 처리 - focus 방은 참여 인원 Redis에 저장
        // disconnect 용 세션id 필요. 하지만 sid로 접속 유저 체크 불가.
        if (groupId != null && userId != null) {
            String redisKey = "focus:" + groupId;
            SetOperations<String, String> setOps = redisTemplate.opsForSet();

            // 세션 id 저장 (세션 종료 시, Redis에서 제거하고 timer도 종료 : groupId, userId로 조회)
            String sessionId = headerAccessor.getSessionId();
            if (sessionId != null) {
                setOps.add("focus:"+sessionId, groupId+":"+userId+":"+nickname);
                redisTemplate.expire(sessionId, Duration.ofDays(1));
            }else {
                // 세션 아이디가 없을 경우 예외 처리
                log.error("SessionId is null");
                throw new RuntimeException("SessionId is null");
            }

            // Redis에 유저 추가
            setOps.add(redisKey, userId+":"+nickname);

            // TTL 설정
            redisTemplate.expire(redisKey, Duration.ofDays(1));

            System.out.println("User " + userId + " joined group 리스너" + groupId);

            // Entry 이벤트 브로드캐스트 객체
            // 가장 최신 timer 객체 불러오기
            TimerDto entryEvent = timerService.getMyTimerByGroupIdAndUserId(Long.parseLong(groupId), Long.parseLong(userId));
            // 타이머가 없는 경우, 새로 입장한 사용자임. Entry 이벤트 객체 생성
            if (entryEvent == null) {
                System.out.println("타이머 없음 새로 입장~");
                entryEvent = new TimerDto();
                entryEvent.setEvent(TimerEvent.ENTRY);
                entryEvent.setUserId(Long.parseLong(userId));
                entryEvent.setNickname(nickname);
                entryEvent.setTimeSoFar(0);
                entryEvent.setStatus("REST");
            }
            // 타이머 있는 경우
            entryEvent.setNickname(nickname);

            // Entry 이벤트 브로드캐스트
            messagingTemplate.convertAndSend("/topic/groups."+groupId+".timers", entryEvent);

            // SSE: 집중방 입장 시 현재 인원수 send
            int focusRoomMemberCount = redisTemplate.opsForSet().members("focus:" + groupId).size();
            studyGroupSseService.sendFocusRoomMemberCountToGroup(Long.parseLong(groupId), focusRoomMemberCount);
        }
    }
}
