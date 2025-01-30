package com.mercury.star_be.chat.controller;

import com.mercury.star_be.chat.dto.request.ChatMessageCountCkRequest;
import com.mercury.star_be.chat.dto.request.ChatMessageRequest;
import com.mercury.star_be.chat.dto.request.CreateChatRoomRequest;
import com.mercury.star_be.chat.dto.response.*;
import com.mercury.star_be.chat.service.ChatService;
import com.mercury.star_be.global.common.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
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

    /**
     * 채팅 메시지 전송 컨트롤러(일반 텍스트)
     * /pub/chat/sendTextMessage/{chatRoomId} 경로로 보낸 메시지를 받음
     * 이 컨트롤러에서 처리된 메시지를 /sub/chat/{chatRoomId} 경로로 구독하고 있는 클라이언트에게 전송
     * */
    @MessageMapping("/chat/sendTextMessage/{chatRoomId}")
    @SendTo("/topic/chat.{chatRoomId}")
    public ApiResponse<ChatMessageResponse> sendChatMessage(
            @Payload ChatMessageRequest chatMessageRequest
    ){
        ChatMessageResponse chatMessageResponse = chatService.sendMessage(chatMessageRequest);
        return ApiResponse.success(chatMessageResponse);
    }

    /**채팅 메시지 전송 컨트롤러(사진 파일)*/
    @MessageMapping("/chat/sendFile/{chatRoomId}")
    @SendTo("/topic/chat.{chatRoomId}")
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
        ChatRoomListResponse chatRoomListResponse = chatService.getUserChatRooms(userId);
        return ApiResponse.success(chatRoomListResponse);
    }

    /**
     * 1:1 채팅 사용자 간의 이전 채팅 메시지 숫자 확인 컨트롤러
     * */
    @GetMapping("/api/chat/chatMessageCountCk")
    public ApiResponse<ChatMessageCountCkResponse> chatMessageCountCk(
        @RequestBody
        ChatMessageCountCkRequest chatMessageCountCkRequest
    ){
        ChatMessageCountCkResponse chatMessageCountCkResponse
                = chatService.findChatMessageRecord(chatMessageCountCkRequest);
        return ApiResponse.success(chatMessageCountCkResponse);
    }

    /**1:1 채팅방 개설 컨트롤러*/
    @PostMapping("/api/chat/createChatRoom")
    public ApiResponse<CreateChatRoomResponse> createChatRoom(
            @RequestBody CreateChatRoomRequest createChatRoomRequest
    ){
        chatService.createChatRoom(createChatRoomRequest);
        CreateChatRoomResponse createChatRoomResponse = CreateChatRoomResponse.builder()
                .result("채팅방이 생성되었습니다.")
                .build();
        return ApiResponse.success(createChatRoomResponse);
    }
    //사용자 차단
    //사용자 차단 해제
    //차단 사용자 목록

}
