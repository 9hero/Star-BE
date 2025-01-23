package com.mercury.star_be.chat.repository;

import com.mercury.star_be.chat.entity.ChatRoom;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {

//    @Query("SELECT cr FROM ChatRoom cr JOIN cr.members m1 JOIN cr.members m2 WHERE cr.chatRoomType = 'DM' AND m1.id = :userId AND m2.id = :otherUserId")
//    Optional<ChatRoom> findDmChatRoom(@Param("userId") Long userId, @Param("otherUserId") Long otherUserId);
    //그룹아이디로 채팅방 조회
    Optional<ChatRoom> findByStudyGroupId(Long groupId);


}
