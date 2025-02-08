package com.mercury.star_be.chat.dto.response;

import com.mercury.star_be.chat.dto.common.ChatRoomMemberDto;
import com.mercury.star_be.user.entity.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;
import java.util.Set;

/**현재 채팅방에 접속해있는 사용자 정보 response 객체*/
@Getter
@Builder
@AllArgsConstructor
public class ChatRoomConnectedUsersResponse {
    private List<ChatRoomMemberDto> chatRoomUsers;
    private Set<String> connectedUsers;
}
