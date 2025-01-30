package com.mercury.star_be.chat.dto.request;

import com.mercury.star_be.chat.entity.ChatRoomType;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
    private Long receiverId;
    @NotBlank
    @Enumerated(EnumType.STRING)
    private ChatRoomType chatRoomType;
}
