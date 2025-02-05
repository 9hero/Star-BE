package com.mercury.star_be.chat.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

/**
 * 그룹 채팅 가입 시 사용
 * */
@Getter
@Builder
@AllArgsConstructor
public class ChatRoomJoinResponse {
    private boolean result;
}
