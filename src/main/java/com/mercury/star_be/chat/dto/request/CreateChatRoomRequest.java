package com.mercury.star_be.chat.dto.request;

import com.mercury.star_be.chat.entity.ChatRoomType;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

/**채팅방 개설용 dto
 * 1:1채팅은 receiverId / 그룹채팅은 groupId가 필요하다.
 * */
@Getter
@Builder
@AllArgsConstructor
public class CreateChatRoomRequest {
    @NotBlank
    private Long senderId;
    private Long receiverId;
    private Long groupId;
}
