package com.mercury.star_be.chat.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.Set;

/**현재 채팅방에 접속한 사용자 정보 request 객체*/
@Getter
@Builder
@AllArgsConstructor
public class ChatRoomConnectedUserRequest {
    private String connectedMemberId;
}
