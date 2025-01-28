package com.mercury.star_be.chat.dto.response;

import com.mercury.star_be.chat.dto.common.ChatRoomMemberDto;
import com.mercury.star_be.chat.dto.common.ChatRoomMessageDto;
import com.mercury.star_be.chat.entity.ChatRoomType;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

/**
 * 채팅방 정보 response 객체
 * 유저가 하나의 채팅방을 들어갈 때 사용
 * DM(1:1 채팅은 studyGroupId값이 null)
 * */
@Getter
@Builder
@AllArgsConstructor
public class ChatRoomResponse {
    //채팅방 멤버 목록
    private List<ChatRoomMemberDto> chatMembers;
    //채팅방 메시지
    List<ChatRoomMessageDto> messages;

    @Enumerated(EnumType.STRING)
    private ChatRoomType chatRoomType;
    private Long studyGroupId;

}
