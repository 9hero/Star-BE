package com.mercury.star_be.chat.dto.response;

import com.mercury.star_be.chat.dto.common.ChatMessageFileDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

/**채팅 메시지 response 객체*/
@Getter
@Builder
@AllArgsConstructor
public class ChatMessageResponse {
    private Long id;
    private String nickName;
    private LocalDateTime createdAt;
    private int unreadCount;
    private String messageContent;
    private String profileImgUrl;
    List<ChatMessageFileDto> messageFiles;
}
