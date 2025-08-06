
package com.mercury.star_be.timer.dto.event;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class FocusRoomDisconnectedEvent {
    private final String sessionId;
    private final Long groupId;
    private final Long userId;
    private final Long groupMemberId;
    private final String nickname;
}
