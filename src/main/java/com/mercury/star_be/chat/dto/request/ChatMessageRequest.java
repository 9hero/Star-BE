package com.mercury.star_be.chat.dto.request;

import com.mercury.star_be.chat.dto.common.ChatMessageFileDto;
import com.mercury.star_be.chat.entity.ChatRoomType;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

/**
 * 채팅메시지 request 객체
 * 일반 채팅메시지 / 파일 업로드 모두 이 객체를 사용
 * 일반 채팅 메시지는 messageFiles가 null이다.
 * */
@Getter
@Builder
@AllArgsConstructor
public class ChatMessageRequest {
    @NotBlank
    private Long chatRoomId;
    @NotBlank
    private Long senderId;
    //그룹은 수신자가 null
    private Long receiverId;
    private String nickName;
    //파일업로드의 경우에는 자동으로 특정 string 넣기(ex : "fileUpload")
    @NotBlank
    @Size(min = 1, max = 500, message = "최소 1자 이상의 채팅을 입력해야합니다.")
    private String messageContent;
    //일반 메시지 채팅은 파일이 null
    List<ChatMessageFileDto> messageFiles;
    @Enumerated(EnumType.STRING)
    private ChatRoomType chatRoomType;

    public void fileUploadContentString(){
        this.messageContent = "fileUpload";
    }

}
