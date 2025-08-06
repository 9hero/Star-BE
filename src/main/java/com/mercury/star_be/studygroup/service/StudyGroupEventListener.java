package com.mercury.star_be.studygroup.service;

import com.mercury.star_be.studygroup.entity.ConnectionStatus;
import com.mercury.star_be.timer.dto.event.FocusRoomConnectedEvent;
import com.mercury.star_be.timer.dto.event.FocusRoomDisconnectedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class StudyGroupEventListener {

    private final StudyGroupSseService studyGroupSseService;
    private final RedisTemplate<String, String> redisTemplate;

    @EventListener
    public void handleFocusRoomConnected(FocusRoomConnectedEvent event) {
        long groupId = Long.parseLong(event.getGroupId());
        int focusRoomMemberCount = redisTemplate.opsForSet().members("focus:" + groupId).size();
        studyGroupSseService.sendFocusRoomMemberCountToGroup(groupId, focusRoomMemberCount);
    }

    @EventListener
    public void handleFocusRoomDisconnected(FocusRoomDisconnectedEvent event) {
        long groupId = event.getGroupId();
        long userId = event.getUserId();
        int focusRoomMemberCount = redisTemplate.opsForSet().members("focus:" + groupId).size();
        studyGroupSseService.sendFocusRoomMemberCountToGroup(groupId, focusRoomMemberCount);
        studyGroupSseService.sendMemberStatusToGroup(groupId, userId, ConnectionStatus.ONLINE);
    }
}
