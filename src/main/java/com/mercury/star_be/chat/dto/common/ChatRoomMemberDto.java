package com.mercury.star_be.chat.dto.common;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

/**
 * 채팅방 : ChatRoomResponse 에서 사용
 * 한 채팅방 내에서의 멤버를 나타내는 객체
 * */
@Getter
@Builder
@AllArgsConstructor
public class ChatRoomMemberDto {
    private Long id;
    private String nickName;
    private String profileImg;
}
