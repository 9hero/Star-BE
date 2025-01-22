package com.mercury.star_be.chat.service;

import com.mercury.star_be.chat.dto.request.ChatMessageRequest;
import com.mercury.star_be.chat.dto.response.ChatMessageResponse;
import com.mercury.star_be.chat.dto.response.ChatRoomListResponse;
import com.mercury.star_be.chat.dto.response.ChatRoomResponse;
import com.mercury.star_be.chat.repository.ChatMessageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ChatServiceImpl implements ChatService {
    private final ChatMessageRepository chatMessageRepository;

    /**채팅방 조회 서비스*/
    @Override
    public ChatRoomResponse getChatRoom(Long chatRoomId) {
        return null;

    }

    /**채팅방 생성 서비스
     * 그룹은 그룹생성 시
     * DM은 상대방 프로필 DM버튼 클릭시
     * 자동으로 채팅방 생성
     * */
    @Override
    public void createChatRoom(List<Long> chatMemberIds) {
        
    }
    
    /**
     * 채팅 전송 서비스
     * 일반 채팅 메시지 or 파일 업로드인지 확인 필요
     * */
    @Override
    public ChatMessageResponse sendMessage(ChatMessageRequest chatMessageRequest) {
        //redis로 채팅 데이터 저장
        //rabbitMQ(메시지 브로커)로 메시지 전달(pub / sub 사용)
        //mysql에 일정 시간마다  redis의 채팅 메시지 이관(이 기능은 따로 빼둘것)
        return null;
    }
    
    /**
     * 사용자의 채팅방목록 불러오기 서비스
     * */
    @Override
    public ChatRoomListResponse getChatRoomList(Long userId) {
        return null;
    }


}
