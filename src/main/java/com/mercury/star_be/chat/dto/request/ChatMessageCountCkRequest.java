package com.mercury.star_be.chat.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

/**
 * 1:1 채팅에서 두 사람의 채팅 메시지 기록을 확인하기 위한 request 객체
 * */
@Getter
@Builder
@AllArgsConstructor
public class ChatMessageCountCkRequest {
    @NotBlank
    private Long senderId;
    @NotBlank
    private Long receiverId;
}
