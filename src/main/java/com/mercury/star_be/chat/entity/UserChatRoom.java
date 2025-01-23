package com.mercury.star_be.chat.entity;

import com.mercury.star_be.user.entity.User;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 사용자가 가입한 채팅방(들)
 * 채팅목록에 나타낼 정보
 * */
@Entity
@Getter
@NoArgsConstructor
@Table(name = "user_chat_room")
public class UserChatRoom {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private LocalDateTime joinedAt;
    private Boolean isBlock;

    @ManyToOne(fetch = FetchType.LAZY)
    private ChatRoom chatRoom;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User chatUser;

    @Builder
    public UserChatRoom(
            LocalDateTime joinedAt,
            Boolean isBlock,
            ChatRoom chatRoom,
            User chatUser) {
        this.joinedAt = joinedAt;
        this.isBlock = isBlock;
        this.chatRoom = chatRoom;
        this.chatUser = chatUser;
    }
}
