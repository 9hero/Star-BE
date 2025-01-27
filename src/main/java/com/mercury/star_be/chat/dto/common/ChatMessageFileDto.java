package com.mercury.star_be.chat.dto.common;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

/**
 * 업로드 파일 DTO
 * 채팅방 파일 전송 때 사용 
 * */
@Getter
@Builder
@AllArgsConstructor
public class ChatMessageFileDto {
    @NotBlank
    private Long id;
    @NotBlank
    private String fileUrl;
    @NotBlank
    private String fileType;
    @NotBlank
    private Long chatMessageId;
}
