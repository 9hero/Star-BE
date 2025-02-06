package com.mercury.star_be.chat.controller;

import com.mercury.star_be.chat.dto.request.*;
import com.mercury.star_be.chat.dto.response.*;
import com.mercury.star_be.chat.service.ChatService;
import com.mercury.star_be.global.common.ApiResponse;
import com.mercury.star_be.user.dto.response.UserResponse;
import com.mercury.star_be.user.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;
    /**
     * 채팅방 조회 컨트롤러
     * */
    @GetMapping("/api/chats/{chatRoomId}")
    public ApiResponse<ChatRoomResponse> getChatRoom(
            @PathVariable(value = "chatRoomId") Long chatRoomId
    ) {
        //채팅방에 소속된 인원인지 확인.
        UserResponse userResponse =
                (UserResponse) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        chatService.isJoinedChatRoom(userResponse.getId(),chatRoomId);
        //채팅방 조회
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

    /**채팅방 내 메시지 읽음 udpate 컨트롤러*/
    @MessageMapping("/readCheck/{chatRoomId}")
    @SendTo("/topic/readCheck.{chatRoomId}")
    public void updateReadUsers(
            @DestinationVariable
            Long chatRoomId,
            @Payload ChatReadRequest chatReadRequest
    ){
        chatService.updateReadCount(chatReadRequest, chatRoomId);

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
    @PostMapping("/api/chat/createDMChatRoom")
    public ApiResponse<CreateChatRoomResponse> createDMChatRoom(
            @RequestBody CreateChatRoomRequest createChatRoomRequest
    ){
        chatService.createDMChatRoom(createChatRoomRequest);
        CreateChatRoomResponse createChatRoomResponse = CreateChatRoomResponse.builder()
                .result("1:1 채팅방이 생성되었습니다.")
                .build();
        return ApiResponse.success(createChatRoomResponse);
    }

    /**그룹 채팅방 개설 컨트롤러*/
    @PostMapping("/api/chat/createGroupChatRoom")
    public ApiResponse<CreateChatRoomResponse> createGroupChatRoom(
            @RequestBody CreateChatRoomRequest createChatRoomRequest
    ){
        chatService.createGroupChatRoom(createChatRoomRequest);
        CreateChatRoomResponse createChatRoomResponse = CreateChatRoomResponse.builder()
                .result("그룹 채팅방이 생성되었습니다.")
                .build();
        return ApiResponse.success(createChatRoomResponse);
    }

    /**채팅방 가입 컨트롤러*/
    @PostMapping("/api/chat/joinChatRoom")
    public ApiResponse<ChatRoomJoinResponse> joinChatRoom(
            @RequestBody ChatRoomJoinRequest chatRoomJoinRequest
    ){
        ChatRoomJoinResponse chatRoomJoinResponse = chatService.joinChatRoom(chatRoomJoinRequest);
        return ApiResponse.success(chatRoomJoinResponse);
    }

    /**한 채팅방의 한 유저가 읽지 않은 모든 메시지 읽음 처리 컨트롤러*/
    @PostMapping("/api/chats/{chatRoomId}/insertAllUnreadChatMessages")
    public ApiResponse<String> insertAllUnreadChatMessages(
            @PathVariable Long chatRoomId,
            @RequestBody
            ChatUpdateReadMessagesRequest chatUpdateReadMessagesRequest
    ){
        UserResponse userResponse =
                (UserResponse) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        chatService.updateAndInsertChatReads(chatUpdateReadMessagesRequest, userResponse.getId(), chatRoomId);
        return ApiResponse.success("success");
    }

    //사용자 차단
    //사용자 차단 해제
    //차단 사용자 목록

}
