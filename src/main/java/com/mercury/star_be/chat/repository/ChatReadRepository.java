package com.mercury.star_be.chat.repository;

import com.mercury.star_be.chat.entity.ChatRead;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChatReadRepository extends JpaRepository<ChatRead, Long> {
    //특정 유저가 채팅메시지를 읽었는지 확인
    boolean existsByChatMessageIdAndChatUserId(Long chatMessageId, Long chatUserId);
}
