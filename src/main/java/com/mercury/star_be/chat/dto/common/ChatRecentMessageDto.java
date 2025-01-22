package com.mercury.star_be.chat.dto.common;

import java.time.LocalDateTime;

/**
 * 채팅목록에서 채팅방의 가장 최신 메시지를 나타내는 DTO
 * */
public class ChatRecentMessageDto {
    private Long id;
    private String nickName;
    private String content;
    private int unreadCount;
    private LocalDateTime createdAt;
}
