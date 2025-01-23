package com.mercury.star_be.chat.entity;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**채팅 메시지의 파일*/
@Entity
@Getter
@NoArgsConstructor
@Table(name = "message_file")
public class ChatMessageFile {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String fileUrl;
    private String fileType;

    @ManyToOne(fetch = FetchType.LAZY)
    private ChatMessage chatMessage;

    @Builder
    public ChatMessageFile(
            String fileUrl,
            String fileType,
            ChatMessage chatMessage
    ) {
        this.fileUrl = fileUrl;
        this.fileType = fileType;
        this.chatMessage = chatMessage;
    }
}
