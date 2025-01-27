package com.mercury.star_be.chat.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class CreateChatRoomRequest {
    @NotBlank
    private Long senderId;
    @NotBlank
    private Long receiverId;
}
