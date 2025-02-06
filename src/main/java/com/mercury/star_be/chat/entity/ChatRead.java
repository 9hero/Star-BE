package com.mercury.star_be.chat.entity;

import com.mercury.star_be.user.entity.User;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**채팅방 채팅 읽음*/
@Entity
@Getter
@NoArgsConstructor
public class ChatRead {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chat_user_id")
    private User chatUser;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chat_message_id")
    private ChatMessage chatMessage;

    private LocalDateTime createdAt;

    @Builder
    public ChatRead(
            User chatUser,
            ChatMessage chatMessage,
            LocalDateTime createdAt
    ) {
        this.chatUser = chatUser;
        this.chatMessage = chatMessage;
        this.createdAt = createdAt;

    }
}
