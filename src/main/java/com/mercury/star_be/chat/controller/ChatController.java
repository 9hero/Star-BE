package com.mercury.star_be.chat.controller;

import com.mercury.star_be.chat.dto.request.ChatMessageRequest;
import com.mercury.star_be.chat.dto.response.ChatMessageResponse;
import com.mercury.star_be.chat.dto.response.ChatRoomListResponse;
import com.mercury.star_be.chat.dto.response.ChatRoomResponse;
import com.mercury.star_be.chat.service.ChatService;
import com.mercury.star_be.global.common.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;

    /**채팅방 조회 컨트롤러*/
    @GetMapping("/api/chats/{chatRoomId}")
    public ApiResponse<ChatRoomResponse> getChatRoom(
            @PathVariable(value = "chatRoomId") Long chatRoomId
    ) {
        ChatRoomResponse chatRoomResponse = chatService.getChatRoom(chatRoomId);
        return ApiResponse.success(chatRoomResponse);
    }

    /**채팅 메시지 전송 컨트롤러(일반 텍스트)*/
    @PostMapping("/api/chats?userId&recipient")
    public ApiResponse<ChatMessageResponse> sendChatMessage(
            @RequestBody
            ChatMessageRequest chatMessageRequest
    ){
        ChatMessageResponse chatMessageResponse = chatService.sendMessage(chatMessageRequest);
        return ApiResponse.success(chatMessageResponse);
    }

    /**채팅 메시지 전송 컨트롤러(사진 파일)*/
    @PostMapping("/api/chats/{chatId}/file-upload")
    public ApiResponse<ChatMessageResponse> uploadChatFile(
            @RequestBody
            ChatMessageRequest chatMessageRequest
    ){
        ChatMessageResponse chatMessageResponse = chatService.sendMessage(chatMessageRequest);
        return ApiResponse.success(chatMessageResponse);
    }

    /**내 채팅방 목록 조회 컨트롤러*/
    @GetMapping("/api/users/{userId}/chats")
    public ApiResponse<ChatRoomListResponse> getChatRoomList(
            @PathVariable
            Long userId
    ){
        ChatRoomListResponse chatRoomListResponse = chatService.getChatRoomList(userId);
        return ApiResponse.success(chatRoomListResponse);
    }

    //사용자 차단
    //사용자 차단 해제
    //차단 사용자 목록

}
