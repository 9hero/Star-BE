package com.mercury.star_be.chat.repository;

import com.mercury.star_be.chat.dto.request.ChatUpdateReadMessagesRequest;

import java.util.List;

/**
 * 채팅 관련 커스텀 레포지토리
 * 두 테이블을 조인해야하거나, 여러 데이터를 한번에 가져올 때 필요한 메서드를 여기에 정의
 * */ 
public interface ChatCustomRepository {
    /**사용자가 채팅방에서 읽지 않은 메시지들의 아이디 리스트*/
    List<Long> findUnreadMessageIds(Long chatRoomId, Long userId);
    /**사용자의 읽지 않은 메시지들 전부 읽음 처리*/
    void insertChatReads(ChatUpdateReadMessagesRequest request, Long userId);
    /**시용자의 읽지 않은 메시지들 unreadCount 전부 -1*/
    void updateChatReads(ChatUpdateReadMessagesRequest request);
}
