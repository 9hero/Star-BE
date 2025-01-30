package com.mercury.star_be.chat.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

/**
 * 그룹 채팅 가입 시 사용
 * */
@Getter
@Builder
@AllArgsConstructor
public class ChatRoomJoinRequest {
    private Long roomId;
    private Long studyGroupId;
    private Long userId;
}
