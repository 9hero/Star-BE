package com.mercury.star_be.chat.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

/**
 * 1:1 채팅에서 두 사람의 채팅 메시지 기록을 확인하기 위한 response 객체
 * */
@Builder
@Getter
@AllArgsConstructor
public class ChatMessageCountCkResponse {
    private Long count;
}
