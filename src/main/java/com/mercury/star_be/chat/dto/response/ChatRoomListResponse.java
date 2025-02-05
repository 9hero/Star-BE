package com.mercury.star_be.chat.dto.response;

import com.mercury.star_be.chat.dto.common.ChatRoomDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

/**사용자의 채팅목록 response 객체 */
@Getter
@Builder
@AllArgsConstructor
public class ChatRoomListResponse {
    private Long userId;
    //채팅방에 대한 정보 + 최근 메시지 정보
    List<ChatRoomDto> chatRooms;
}
