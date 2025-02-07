package com.mercury.star_be.chat.entity;

import com.mercury.star_be.studygroup.entity.StudyGroup;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**채팅방 정보*/
@Entity
@Getter
@NoArgsConstructor
public class ChatRoom {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Enumerated(EnumType.STRING)
    private ChatRoomType chatRoomType;
    private LocalDateTime createdAt;

    @OneToOne
    @JoinColumn(name = "group_id")
    private StudyGroup studyGroup;

    @OneToMany(mappedBy = "chatRoom", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<UserChatRoom> userChatRooms;

    @OneToMany(mappedBy = "chatRoom", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ChatMessage> chatMessages;

    @Builder
    public ChatRoom(
            ChatRoomType chatRoomType,
            LocalDateTime createdAt,
            StudyGroup studyGroup,
            List<UserChatRoom> userChatRooms
    ) {
        this.chatRoomType = chatRoomType;
        this.createdAt = createdAt;
        this.studyGroup = studyGroup;
        this.userChatRooms = userChatRooms != null ? userChatRooms : List.of();

    }

    public void insertUserChatRooms(UserChatRoom userChatRoom) {
        this.userChatRooms.remove(userChatRoom);
    }

}
