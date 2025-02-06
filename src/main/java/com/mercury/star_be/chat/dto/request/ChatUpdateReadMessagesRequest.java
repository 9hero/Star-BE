package com.mercury.star_be.chat.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

/**읽지 않은 메시지들의 id를 전부 받는 request 객체*/
@Getter
@Builder
@AllArgsConstructor
public class ChatUpdateReadMessagesRequest {
    private List<Long> unreadMessages;
}
