package com.mercury.star_be.timer.controller;

import com.mercury.star_be.timer.dto.TimerDto;
import com.mercury.star_be.timer.dto.TimerEvent;
import com.mercury.star_be.timer.service.TimerServiceImpl;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitMessagingTemplate;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class TimerController {

    private final RabbitMessagingTemplate rabbitTemplate;
    private final SimpMessagingTemplate messagingTemplate;
    private final TimerServiceImpl timerServiceImpl;
    private final RedisTemplate<String, Object> redisTemplate;

    // 집중방 입장한 사용자들의 타이머 정보를 가져옴
    @GetMapping("/api/groups/{groupId}/timers")
    public Set<TimerDto> getTimerData(@PathVariable Long groupId) {
        System.out.println("groupId: " + groupId + " i got it from session! :");
        return timerServiceImpl.getFocusRoomTimerDataByGroupId(groupId);
    }

    // 클라이언트가 메시지를 보낼 때 처리
    @MessageMapping("/groups/{groupId}/timers") // 클라이언트에서 "/pub/groups/{groupId}/timers"로 보낸 메시지를 처리
    @SendTo("/topic/groups.{groupId}.timers") // "/topic/groups/{groupId}/timers"로 구독한 사용자들에게 메시지를 전송
    public String handleTimerUpdate(@DestinationVariable Long groupId,String message) {
        System.out.println("Received message: " + message+" from group: " + groupId);
        System.out.println(" 통과는 함?");
        return "Updated timer : " + message; // 클라이언트로 전송할 메시지
    }

    // 집중방 클라이언트가 타이머 시작을 알림
    @MessageMapping("/groups/{groupId}/timers/start")
    @SendTo("/topic/groups.{groupId}.timers")
    public TimerDto handleTimerStart(@DestinationVariable Long groupId,Long userId) {
        System.out.println("타이머 시작 요청 받음");
//        TimerDto resultDTO = timerServiceImpl.startMyTimer(groupId, userId);
//        rabbitTemplate.convertAndSend("/topic/groups/"+groupId+"/timers", resultDTO);
        return timerServiceImpl.startMyTimer(groupId,userId);
//        return null;
    }
    // 집중방 클라이언트가 타이머 일시 중지를 알림
    @MessageMapping("/groups/{groupId}/timers/stop")
    @SendTo("/topic/groups.{groupId}.timers")
    public TimerDto handleTimerStop(@DestinationVariable Long groupId, Long userId) {
        System.out.println("타이머 일시 중지 요청 받음");
        return timerServiceImpl.stopTimerByGroupIdAndUserId(groupId,userId);
    }
    // 집중방 클라이언트가 타이머 종료를 알림
    @MessageMapping("/groups/{groupId}/timers/end")
    @SendTo("/topic/groups.{groupId}.timers")
    public TimerDto handleTimerEnd(@DestinationVariable Long groupId, Long userId) {
        return timerServiceImpl.endTimerByGroupIdAndUserId(groupId,userId);
    }

    // 사용자가 직접 집중방을 나갈 때 처리
    @MessageMapping("/api/groups/{groupId}/focusRoom/disconnect")
    @SendTo("/topic/groups.{groupId}.timers")
    public TimerDto handleDisconnect(@DestinationVariable Long groupId, Long userId, SimpMessageHeaderAccessor headerAccessor) {
        if (groupId != null && userId != null) {
            // 일시정지 상태에서 나갔다가 돌아와도, 지금까지 공부한 시간 유지하기 위함
            // 해당 사용자의 타이머 정보를 가져와서, 해당 사용자의 타이머 정보를 업데이트
            timerServiceImpl.stopTimerByGroupIdAndUserId(groupId,userId);

            // Redis 세션 제거
            Boolean delete = redisTemplate.delete(headerAccessor.getSessionId());
            System.out.println("세션 삭제 성공? " + delete);

            // 브로드캐스트
            TimerDto disconnectEvent = new TimerDto();
            disconnectEvent.setUserId(userId);
            disconnectEvent.setEvent(TimerEvent.DISCONNECT);

            return disconnectEvent;
        }
        return null;
    }

}
