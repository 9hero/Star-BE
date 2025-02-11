package com.mercury.star_be.chat.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

/**채팅방의 최신 메시지를 채팅목록으로 보내는데 사용되는 request DTO*/
@Getter
@Builder
@AllArgsConstructor
public class ChatRecentMessageRequest {
    private Long id;
    private Long senderId;
    private Long chatRoomId;
    private String nickName;
    private String profileImgUrl;
    private String content;
    private LocalDateTime createdAt;

}
