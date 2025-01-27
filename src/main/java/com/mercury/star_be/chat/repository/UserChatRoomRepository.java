package com.mercury.star_be.chat.repository;


import com.mercury.star_be.chat.entity.UserChatRoom;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserChatRoomRepository extends JpaRepository<UserChatRoom, Long> {
    Optional<List<UserChatRoom>> findByChatUserId(Long chatUserId);
}
