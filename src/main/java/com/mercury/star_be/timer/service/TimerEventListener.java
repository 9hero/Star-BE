package com.mercury.star_be.timer.service;

import com.mercury.star_be.timer.dto.TimerDto;
import com.mercury.star_be.timer.dto.TimerEvent;
import com.mercury.star_be.timer.dto.event.FocusRoomConnectedEvent;
import com.mercury.star_be.timer.dto.event.FocusRoomDisconnectedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SetOperations;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
@RequiredArgsConstructor
public class TimerEventListener {

    private final TimerService timerService;
    private final RedisTemplate<String, String> redisTemplate;
    private final SimpMessagingTemplate messagingTemplate;

    @EventListener
    public void handleFocusRoomConnected(FocusRoomConnectedEvent event) {
        long groupId = Long.parseLong(event.getGroupId());
        long groupMemberId = Long.parseLong(event.getGroupMemberId());
        long userId = Long.parseLong(event.getUserId());

        // 1. Redis에 사용자 접속 정보 저장
        String redisKey = "focus:" + groupId;
        SetOperations<String, String> setOps = redisTemplate.opsForSet();
        setOps.add("focus:" + event.getSessionId(), event.getGroupId() + ":" + userId + ":" + groupMemberId + ":" + event.getNickname());
        redisTemplate.expire("focus:" + event.getSessionId(), Duration.ofDays(1));
        setOps.add(redisKey, groupMemberId + ":" + event.getNickname());
        redisTemplate.expire(redisKey, Duration.ofDays(1));

        // 2. Entry 이벤트 브로드캐스트 객체 생성
        TimerDto entryEvent = timerService.getMyRecentTimerByGroupMemberId(groupMemberId);
        if (entryEvent == null) {
            entryEvent = new TimerDto();
            entryEvent.setEvent(TimerEvent.ENTRY);
            entryEvent.setUserId(userId);
            entryEvent.setGroupMemberId(groupMemberId);
            entryEvent.setNickname(event.getNickname());
            entryEvent.setTimeSoFar(0);
            entryEvent.setStatus("REST");
        } else {
            entryEvent.setNickname(event.getNickname());
        }

        // 3. Entry 이벤트 브로드캐스트
        messagingTemplate.convertAndSend("/topic/groups." + groupId + ".timers", entryEvent);
    }

    @EventListener
    public void handleFocusRoomDisconnected(FocusRoomDisconnectedEvent event) {
        // 1. 타이머 정지 처리
        timerService.stopTimerByGroupMemberId(event.getGroupMemberId());

        // 2. Redis에서 사용자 정보 삭제
        redisTemplate.opsForSet().remove("focus:" + event.getSessionId(), event.getGroupId() + ":" + event.getUserId() + ":" + event.getGroupMemberId() + ":" + event.getNickname());
        redisTemplate.opsForSet().remove("focus:" + event.getGroupId(), event.getGroupMemberId() + ":" + event.getNickname());

        // 3. Disconnection 브로드캐스트
        TimerDto disconnectEvent = new TimerDto();
        disconnectEvent.setUserId(event.getUserId());
        disconnectEvent.setEvent(TimerEvent.DISCONNECT);
        messagingTemplate.convertAndSend("/topic/groups." + event.getGroupId() + ".timers", disconnectEvent);
    }
}
