package com.mercury.star_be.timer.dto.event;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class FocusRoomConnectedEvent {
    private final String sessionId;
    private final String groupId;
    private final String groupMemberId;
    private final String userId;
    private final String nickname;
}
