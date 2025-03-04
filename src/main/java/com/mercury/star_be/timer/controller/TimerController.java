package com.mercury.star_be.timer.controller;

import com.mercury.star_be.studygroup.entity.ConnectionStatus;
import com.mercury.star_be.studygroup.service.StudyGroupSseService;
import com.mercury.star_be.timer.dto.EventMessage;
import com.mercury.star_be.timer.dto.TimerDto;
import com.mercury.star_be.timer.dto.TimerEvent;
import com.mercury.star_be.timer.service.TimerService;

import java.util.Objects;
import java.util.Set;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class TimerController {

    private final TimerService timerService;
    private final StudyGroupSseService studyGroupSseService;
    private final RedisTemplate<String, Object> redisTemplate;

    // 집중방 입장 했음을 Redis에 저장하고 타이머 정보를 가져옴
    // 닉네임 그룹방에 해당하는 거로 조회해서 저장하기.
    @GetMapping("/api/timers/groups/{groupId}/entry")
    public TimerDto enterFocusRoom(@PathVariable Long groupId) {
        return timerService.enterFocusRoom(groupId);
    }

    // 집중방 입장한 사용자들의 타이머 정보를 가져옴
    @GetMapping("/api/timers/groups/{groupId}")
    public Set<TimerDto> getTimerData(@PathVariable Long groupId) {
        return timerService.getFocusRoomTimerDataByGroupIdAndMyUID(groupId);
    }

    // 집중방 클라이언트가 타이머 시작을 알림
    @MessageMapping("/groups/{groupId}/timers/start")
    @SendTo("/topic/groups.{groupId}.timers")
    public TimerDto handleTimerStart(@DestinationVariable Long groupId,Long groupMemberId, Long userId) {
        // START 이벤트 로직 수행
        return timerService.startMyTimer(groupId, userId);
    }
    // 집중방 클라이언트가 타이머 일시 중지를 알림
    @MessageMapping("/groups/{groupId}/timers/stop")
    @SendTo("/topic/groups.{groupId}.timers")
    public TimerDto handleTimerStop(@DestinationVariable Long groupId, Long userId) {
        return timerService.stopTimerByGroupMemberId(userId);
    }
    // 집중방 클라이언트가 타이머 종료를 알림
    @MessageMapping("/groups/{groupId}/timers/end")
    @SendTo("/topic/groups.{groupId}.timers")
    public TimerDto handleTimerEnd(@DestinationVariable Long groupId, Long userId) {
        return timerService.endTimerByGroupIdAndUserId(groupId,userId);
    }

    // 집중방 클라이언트가 타이머 이벤트를 전송 (START, STOP, END)
    @MessageMapping("/groups/{groupId}/timers")
    @SendTo("/topic/groups.{groupId}.timers")
    public TimerDto handleTimerEvent(@DestinationVariable Long groupId, EventMessage eventMessage) {
        // PK
        Long userId = eventMessage.getUserId();
        Long groupMemberId = eventMessage.getGroupMemberId();

        return switch (eventMessage.getTimerEvent()) {
            // 이벤트 로직 수행,
            // SSE 유저 상태 메시지 전송.
            // return dto.
            case START -> {
                TimerDto dto = timerService.startMyTimer(groupId, groupMemberId);
                studyGroupSseService.sendMemberStatusToGroup(groupId, userId, ConnectionStatus.STUDYING);
                yield dto;
            }
            case STOP -> {
                TimerDto dto = timerService.stopTimerByGroupMemberId(groupMemberId);
                studyGroupSseService.sendMemberStatusToGroup(groupId, userId, ConnectionStatus.RESTING);
                yield dto;
            }
            case END -> {
                TimerDto dto = timerService.endTimerByGroupIdAndUserId(groupId, groupMemberId);
                studyGroupSseService.sendMemberStatusToGroup(groupId, userId, ConnectionStatus.ONLINE);
                yield dto;
            }
            default -> throw new IllegalArgumentException("Invalid event type: " + eventMessage.getTimerEvent());
        };
    }

    // 사용자가 직접 집중방을 나갈 때 처리
    @MessageMapping("/api/groups/{groupId}/focusRoom/disconnect")
    @SendTo("/topic/groups.{groupId}.timers")
    public TimerDto handleDisconnect(@DestinationVariable Long groupId, Long groupMemberId, SimpMessageHeaderAccessor headerAccessor) {
        if (groupId != null && groupMemberId != null) {
            // 일시정지 상태에서 나갔다가 돌아와도, 지금까지 공부한 시간 유지하기 위함
            // 해당 사용자의 타이머 정보를 가져와서, 해당 사용자의 타이머 정보를 업데이트
            timerService.stopTimerByGroupMemberId(groupMemberId);

            // Redis 세션 제거
            Boolean delete = redisTemplate.delete(
                Objects.requireNonNull(headerAccessor.getSessionId()));
            System.out.println("세션 삭제 성공? " + delete);

            // 브로드캐스트
            TimerDto disconnectEvent = new TimerDto();
            disconnectEvent.setUserId(groupMemberId);
            disconnectEvent.setEvent(TimerEvent.DISCONNECT);

            return disconnectEvent;
        }
        return null;
    }

}
