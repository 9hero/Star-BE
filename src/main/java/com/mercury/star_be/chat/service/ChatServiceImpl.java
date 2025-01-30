package com.mercury.star_be.chat.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mercury.star_be.chat.dto.common.*;
import com.mercury.star_be.chat.dto.request.ChatMessageCountCkRequest;
import com.mercury.star_be.chat.dto.request.ChatMessageRequest;
import com.mercury.star_be.chat.dto.request.CreateChatRoomRequest;
import com.mercury.star_be.chat.dto.response.ChatMessageCountCkResponse;
import com.mercury.star_be.chat.dto.response.ChatMessageResponse;
import com.mercury.star_be.chat.dto.response.ChatRoomListResponse;
import com.mercury.star_be.chat.dto.response.ChatRoomResponse;
import com.mercury.star_be.chat.entity.*;
import com.mercury.star_be.chat.repository.ChatMessageRepository;
import com.mercury.star_be.chat.repository.UserChatRoomRepository;
import com.mercury.star_be.chat.repository.ChatRoomRepository;
import com.mercury.star_be.global.error.BusinessException;
import com.mercury.star_be.global.error.code.ChatErrorCode;
import com.mercury.star_be.studygroup.entity.GroupMember;
import com.mercury.star_be.user.entity.User;
import com.mercury.star_be.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitMessagingTemplate;
import org.springframework.messaging.MessagingException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ChatServiceImpl implements ChatService {

    private final ChatMessageRepository chatMessageRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final UserChatRoomRepository userChatRoomRepository;
    private final UserRepository userRepository;
    private final RabbitMessagingTemplate messagingTemplate;
    private final ObjectMapper objectMapper;

    /**
     * 채팅방 조회
     */
    @Override
    public ChatRoom findByChatRoomId(Long chatRoomId) {
        return chatRoomRepository.findById(chatRoomId).orElseThrow(
                () -> new BusinessException(ChatErrorCode.CHAT_ROOM_NOT_FOUND)
        );
    }

    /**
     * 채팅룸 정보를 조회하여 ChatRoomResponse로 return
     */
    @Override
    public ChatRoomResponse getChatRoom(Long chatRoomId) {
        ChatRoom chatRoom = findByChatRoomId(chatRoomId);
        Long studyGroupId = null;
        if (chatRoom.getChatRoomType().equals(ChatRoomType.GROUP)) {
            studyGroupId = chatRoom.getStudyGroup().getId();
        }

        ChatRoomResponse chatRoomResponse = ChatRoomResponse.builder()
        //채팅방 멤버 (DM은 발신자 수신자 / 그룹은 그룹멤버들)
        .chatMembers(getChatRoomMembers(chatRoomId))
        .messages(getChatRoomMessageDtos(chatRoomId))
        .chatRoomType(chatRoom.getChatRoomType())
        .studyGroupId(studyGroupId)
        .build();
        return chatRoomResponse;
    }

    /**
     * 채팅방 id를 받아
     * List<ChatRoomMessageDto>로 return
     */
    public List<ChatRoomMessageDto> getChatRoomMessageDtos(Long chatRoomId) {
        List<ChatMessage> chatMessages = findChatRoomMessages(chatRoomId);
        List<ChatRoomMessageDto> chatRoomMessageDtos = new ArrayList<>();
        for (ChatMessage chatMessage : chatMessages) {
            ChatRoomMessageDto chatRoomMessageDto = ChatRoomMessageDto.builder()
                    .id(chatMessage.getId())
                    .senderId(chatMessage.getChatSender().getId())
                    .nickName(chatMessage.getChatSender().getNickname())
                    .content(chatMessage.getContent())
                    .unreadCount(chatMessage.getUnreadCount())
                    .createdAt(chatMessage.getCreatedAt())
                    .messageFiles(getChatMessageFileDtos(chatMessage.getChatMessageFiles()))
                    .build();
        }
        return chatRoomMessageDtos;
    }

    /**
     * List<ChatMessageFile> chatMessageFiles를 받아
     * List<ChatMessageFileDto>로 return
     */
    public List<ChatMessageFileDto> getChatMessageFileDtos(List<ChatMessageFile> chatMessageFiles) {
        List<ChatMessageFileDto> chatMessageFileDtos = new ArrayList<>();
        for (ChatMessageFile chatMessageFile : chatMessageFiles) {
            ChatMessageFileDto chatMessageFileDto = ChatMessageFileDto.builder()
                    .id(chatMessageFile.getId())
                    .fileUrl(chatMessageFile.getFileUrl())
                    .fileType(chatMessageFile.getFileType())
                    .chatMessageId(chatMessageFile.getId())
                    .build();
            chatMessageFileDtos.add(chatMessageFileDto);
        }
        return chatMessageFileDtos;
    }

    /**
     * 1:1채팅에서 두 사용자 간의 이전 채팅 기록 count 확인 서비스
     */


    /**
     * 채팅 전송 서비스
     * 일반 채팅 메시지 / 파일 업로드
     */
    @Override
    public ChatMessageResponse sendMessage(ChatMessageRequest chatMessageRequest) {
        // Redis에 채팅 데이터 저장

        // STOMP 메시지를 받아 RabbitMQ로 메시지 전달
        ChatRoom chatRoom = findByChatRoomId(chatMessageRequest.getChatRoomId());

        //구독자에게 메시지 전달
        try {
            String messageJson = objectMapper.writeValueAsString(chatMessageRequest);
            messagingTemplate.convertAndSend("/topic/chat/" + chatMessageRequest.getChatRoomId(), messageJson);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            throw new BusinessException(ChatErrorCode.CHAT_MESSAGE_CONVERT_ERROR);
        } catch (MessagingException e) {
            e.printStackTrace();
            throw new BusinessException(ChatErrorCode.MESSAGE_SENDING_ERROR);
        }


        // 메시지가 파일 업로드라면
        if (chatMessageRequest.getMessageFiles() != null && !chatMessageRequest.getMessageFiles().isEmpty()) {
            // 파일 업로드인 경우, "fileUpload" 라는 String이 messageContent로 입력됨
            chatMessageRequest.fileUploadContentString();
        }

        //읽지 않은 메시지 수
        //DM : 1
        //GROUP : 그룹멤버수
        int unreadCount = 1;
        if (chatRoom.getChatRoomType() == ChatRoomType.GROUP) {
            unreadCount = chatRoom.getStudyGroup().getMemberCount();
        }
        // ChatMessageResponse 생성
        ChatMessageResponse response = ChatMessageResponse.builder()
                .createdAt(LocalDateTime.now())
                .unreadCount(unreadCount) // 읽지 않은 메시지 수 (기본값 : 채팅방인원)
                .messageContent(chatMessageRequest.getMessageContent())
                .messageFiles(chatMessageRequest.getMessageFiles()) // 파일 정보 추가
                .build();

        return response;
    }

    /**
     * 채팅방 생성 서비스
     * 1:1 채팅일 때 사용
     */
    @Override
    public void createChatRoom(CreateChatRoomRequest createChatRoomRequest) {
        User sender =
                userRepository.findById(createChatRoomRequest.getSenderId()).orElseThrow(
                        () -> new RuntimeException("송신자를 찾지 못합니다.")
                );
        User receiver =
                userRepository.findById(createChatRoomRequest.getReceiverId()).orElseThrow(
                        () -> new RuntimeException("수신자를 찾지 못합니다.")
                );

        // 채팅방 새로 생성
        ChatRoom chatRoom = ChatRoom.builder()
                .chatRoomType(ChatRoomType.DM)
                .createdAt(LocalDateTime.now())
                .studyGroup(null)
                .userChatRooms(sender.getUserChatRooms())
                .build();
        
        //채팅방 저장
        chatRoomRepository.save(chatRoom);

        // 사용자 채팅방 생성(송신자 / 수신자)
        UserChatRoom senderUserChatRoom = UserChatRoom.builder()
                .joinedAt(LocalDateTime.now())
                .isBlock(false)
                .chatRoom(chatRoom)
                .chatUser(sender)
                .build();
        UserChatRoom receiverUserChatRoom = UserChatRoom.builder()
                .joinedAt(LocalDateTime.now())
                .isBlock(false)
                .chatRoom(chatRoom)
                .chatUser(receiver)
                .build();
        
        //사용자 채팅목록 저장
        userChatRoomRepository.save(senderUserChatRoom);
        userChatRoomRepository.save(receiverUserChatRoom);


    }

    /**
     * 사용자의 채팅방 목록 불러오기 서비스
     * 사용자의 채팅 목록을 불러온 뒤, 채팅방들의 id값을 얻어내어
     * 채팅방 정보를 가져와 response dto로 반환
     */
    @Override
    public ChatRoomListResponse getUserChatRooms(Long userId) {
        Optional<List<UserChatRoom>> optionalUserChatRooms = userChatRoomRepository.findByChatUserId(userId);
        if (optionalUserChatRooms.isPresent()) {
            List<UserChatRoom> userChatRooms = optionalUserChatRooms.get();
            List<ChatRoomDto> chatRooms = new ArrayList<>();
            //리스트에 있는 값들에서 chatRoomId로 ChatRooms 찾아내기
            for (UserChatRoom userChatRoom : userChatRooms) {
                Long chatRoomId = userChatRoom.getChatRoom().getId();
                ChatRoom chatRoom = findByChatRoomId(chatRoomId);
                chatRooms.add(fromChatRoomEntity(chatRoom));
            }
            ChatRoomListResponse chatRoomListResponse = ChatRoomListResponse.builder()
                    .userId(userId)
                    .chatRooms(chatRooms)
                    .build();
            return chatRoomListResponse;
        } else {
            return ChatRoomListResponse.builder().build();
        }
    }

    /**
     * 채팅방의 채팅목록 불러오기 서비스
     */
    @Override
    public List<ChatMessage> findChatRoomMessages(Long chatRoomId) {
        Optional<List<ChatMessage>> optionalChatMessages =
                chatMessageRepository.findByChatRoomId(chatRoomId);
        List<ChatMessage> chatMessages = new ArrayList<>();
        if (optionalChatMessages.isPresent()) {
            chatMessages = optionalChatMessages.get();

        }
        return chatMessages;
    }

    /**
     * 최신메시지 가져오기 서비스
     */
    public ChatRecentMessageDto findRecentMessage(Long chatRoomId) {
        ChatMessage chatMessage =
                chatMessageRepository.findFirstByChatRoomIdOrderByCreatedAtDesc(chatRoomId).orElseThrow(
                        () -> new BusinessException(ChatErrorCode.CHAT_MESSAGE_NOT_FOUND)
                );
        return ChatRecentMessageDto.builder()
                .id(chatMessage.getId())
                //nickName은 추후 userRepository에서 가져옴
                .nickName("test")
                .content(chatMessage.getContent())
                .unreadCount(chatMessage.getUnreadCount())
                .createdAt(chatMessage.getCreatedAt())
                .build();
    }

    /**
     * 채팅방 entity -> dto로 변환 서비스
     */
    public ChatRoomDto fromChatRoomEntity(ChatRoom chatRoom) {
        return ChatRoomDto.builder()
                .id(chatRoom.getId())
                .chatRoomType(chatRoom.getChatRoomType())
                .groupId(chatRoom.getId())
                .recentMessage(findRecentMessage(chatRoom.getId()))
                .build();
    }
    
    /**
     * 1:1채팅에서 두 사용자 간의 이전 채팅 기록 count 확인 서비스
     * */
    @Override
    public ChatMessageCountCkResponse findChatMessageRecord(ChatMessageCountCkRequest chatMessageCountCkRequest) {
        ChatMessageCountCkResponse response = ChatMessageCountCkResponse.builder()
                .count(
                        chatMessageRepository
                                .countByChatSenderIdAndChatReceiverId(
                                        chatMessageCountCkRequest.getSenderId(),
                                        chatMessageCountCkRequest.getReceiverId()
                                )
                )
                .build();
        return response;
    }

    /**
     * 채팅방 id를 받아 List<ChatRoomMemberDto>로 return 서비스
     */
    public List<ChatRoomMemberDto> getChatRoomMembers(Long chatRoomId) {

        ChatRoom chatRoom = findByChatRoomId(chatRoomId);
        List<ChatRoomMemberDto> chatRoomMembers = new ArrayList<>();

        //1:1 채팅의 경우 사용자 채팅방에서 가져오기
        //jwt 관련 코드 update 시 변경필요
        if (chatRoom.getChatRoomType().equals(ChatRoomType.DM)) {
            List<UserChatRoom> userChatRooms =
                    userChatRoomRepository.findByChatRoomId(chatRoomId)
                            .orElseThrow(
                                    ()->new BusinessException(ChatErrorCode.USER_CHAT_ROOM_NOT_FOUND)
                    );
            for (UserChatRoom userChatRoom : userChatRooms) {
                ChatRoomMemberDto chatRoomMemberDto = ChatRoomMemberDto.builder()
                        .id(userChatRoom.getChatUser().getId())
                        .nickName(userChatRoom.getChatUser().getNickname())
                        .profileImg(userChatRoom.getChatUser().getImage())
                        .build();
                chatRoomMembers.add(chatRoomMemberDto);
            }
        } else {

            for (GroupMember groupMember : chatRoom.getStudyGroup().getMembers()) {
                ChatRoomMemberDto chatRoomMemberDto = ChatRoomMemberDto.builder()
                        .id(groupMember.getId())
                        .nickName(groupMember.getNickname())
                        .profileImg(groupMember.getImage())
                        .build();
                chatRoomMembers.add(chatRoomMemberDto);
            }
        }

        return chatRoomMembers;
    }
}
