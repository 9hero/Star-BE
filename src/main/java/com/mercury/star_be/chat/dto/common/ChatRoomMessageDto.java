package com.mercury.star_be.chat.dto.common;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 채팅방에서 보여줄 메시지 내용 DTO
 * 일반 채팅 메시지는 messageFiles가 null
 * */
@Getter
@Builder
@AllArgsConstructor
public class ChatRoomMessageDto {
    private Long id;
    private Long senderId;
    private String nickName;
    private String profileImgUrl;
    private String content;
    private int unreadCount;
    private LocalDateTime createdAt;
    private List<ChatMessageFileDto> messageFiles;

}
