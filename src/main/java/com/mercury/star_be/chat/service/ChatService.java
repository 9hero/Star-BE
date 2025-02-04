package com.mercury.star_be.chat.service;

import com.mercury.star_be.chat.dto.common.ChatRecentMessageDto;
import com.mercury.star_be.chat.dto.common.ChatRoomDto;
import com.mercury.star_be.chat.dto.common.ChatRoomMemberDto;
import com.mercury.star_be.chat.dto.request.*;
import com.mercury.star_be.chat.dto.response.*;
import com.mercury.star_be.chat.entity.ChatMessage;
import com.mercury.star_be.chat.entity.ChatRoom;

import java.util.List;

public interface ChatService {
    //채팅방 조회
    ChatRoom findByChatRoomId(Long chatRoomId);
    ChatRoomResponse getChatRoom(Long chatRoomId);

    //채팅방 생성(그룹채팅방이면 그룹원들 id / DM이면 상대방 id가 필요)
    void createDMChatRoom(CreateChatRoomRequest createChatRoomRequest);
    void createGroupChatRoom(CreateChatRoomRequest createChatRoomRequest);
    //채팅메시지 전송
    ChatMessageResponse sendMessage(ChatMessageRequest chatMessageRequest);
    //사용자 채팅목록 조회
    ChatRoomListResponse getUserChatRooms(Long userId);
    //채팅방 메시지 가져오기
    List<ChatMessage> findChatRoomMessages(Long chatRoomId);
    //최신메시지 가져오기
    ChatRecentMessageDto findRecentMessage(Long chatRoomId, Long userId);
    //채팅방 entity -> dto로 변환
    ChatRoomDto fromChatRoomEntity(ChatRoom chatRoom, Long userId);
    //1:1채팅에서 두 사용자 간의 이전 채팅 기록 count 확인
    ChatMessageCountCkResponse findChatMessageRecord(ChatMessageCountCkRequest chatMessageCountCkRequest);
    //그룹채팅 가입
    ChatRoomJoinResponse joinChatRoom(ChatRoomJoinRequest chatRoomJoinRequest);
    //사용자 채팅방 조회(사용자 아이디, 채팅방 아이디)
    boolean isJoinedChatRoom(Long chatUserId, Long chatRoomId);
    //채팅방 id를 받아 List<ChatRoomMemberDto>로 return
    List<ChatRoomMemberDto> getChatRoomMembers(ChatRoom chatRoom);
    //읽음 update
    void updateReadCount(ChatReadRequest chatReadRequest, Long chatRoomId);
}
