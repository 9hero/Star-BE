package com.mercury.star_be.chat.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
@AllArgsConstructor
public class CreateDmChatRoomResponse {
    private Long chatRoomId;
}
