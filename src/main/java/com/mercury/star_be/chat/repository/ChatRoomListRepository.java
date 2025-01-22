package com.mercury.star_be.chat.repository;

import com.mercury.star_be.chat.entity.ChatRoom;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChatRoomListRepository extends JpaRepository<ChatRoom, Long> {
}
