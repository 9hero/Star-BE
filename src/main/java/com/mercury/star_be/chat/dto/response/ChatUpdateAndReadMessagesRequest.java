package com.mercury.star_be.chat.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

/**읽음처리된 id를 전부 보내는 response 객체*/
@Getter
@Builder
@AllArgsConstructor
public class ChatUpdateAndReadMessagesRequest {
    private List<Long> updatedMessageIds;
}
