package com.mercury.star_be.chat.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

/**채팅 메시지 response 객체*/
@Getter
@Builder
@AllArgsConstructor
public class ChatMessageResponse {
    private Long id;
    private LocalDateTime createdAt;
    private int unreadCount;
}
