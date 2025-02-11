package com.mercury.star_be.chat.repository;

import com.mercury.star_be.chat.entity.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {
    //채팅 메시지 목록을 가져오기
    Optional<List<ChatMessage>> findByChatRoomId(Long chatRoomId);
    // 가장 최신의 채팅 메시지 하나를 가져오기
    Optional<ChatMessage> findFirstByChatRoomIdOrderByCreatedAtDesc(Long chatRoomId);
    //두사람 간의 채팅 기록 count
    int countByChatSenderIdAndChatReceiverId(Long senderId, Long receiverId);
    //한 채팅방 채팅메시지 갯수 확인
    int countByChatRoomId(Long chatRoomId);
}
