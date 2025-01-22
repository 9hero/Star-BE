package com.mercury.star_be.timer.controller;

import com.mercury.star_be.timer.dto.TimerDto;
import com.mercury.star_be.timer.service.TimerService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class TimerController {

    private final SimpMessagingTemplate messagingTemplate;
    private final TimerService timerService;

    @GetMapping("/api/groups/{groupId}/timers")
    public List<TimerDto> getTimerData(@PathVariable Long groupId) {
        System.out.println("groupId: " + groupId);
        return timerService.getFocusRoomTimerDataByGroupId(groupId);
    }

    // 클라이언트가 메시지를 보낼 때 처리
    @MessageMapping("/groups/{groupId}/timers") // 클라이언트에서 "/pub/groups/{groupId}/timers"로 보낸 메시지를 처리
    @SendTo("/sub/groups/{groupId}/timers") // "/sub/groups/{groupId}/timers"로 구독한 사용자들에게 메시지를 전송
    public String handleTimerUpdate(@DestinationVariable Long groupId,String message) {
        System.out.println("Received message: " + message+" from group: " + groupId);
        return "Updated timer : " + message; // 클라이언트로 전송할 메시지
    }

}
