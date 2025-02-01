package com.mercury.star_be.chat.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mercury.star_be.chat.dto.common.*;
import com.mercury.star_be.chat.dto.request.ChatMessageCountCkRequest;
import com.mercury.star_be.chat.dto.request.ChatMessageRequest;
import com.mercury.star_be.chat.dto.request.ChatRoomJoinRequest;
import com.mercury.star_be.chat.dto.request.CreateChatRoomRequest;
import com.mercury.star_be.chat.dto.response.*;
import com.mercury.star_be.chat.entity.*;
import com.mercury.star_be.chat.repository.ChatMessageFileRepository;
import com.mercury.star_be.chat.repository.ChatMessageRepository;
import com.mercury.star_be.chat.repository.UserChatRoomRepository;
import com.mercury.star_be.chat.repository.ChatRoomRepository;
import com.mercury.star_be.global.error.BusinessException;
import com.mercury.star_be.global.error.code.ChatErrorCode;
import com.mercury.star_be.global.error.code.StudyGroupErrorCode;
import com.mercury.star_be.global.error.code.UserErrorCode;
import com.mercury.star_be.studygroup.entity.GroupMember;
import com.mercury.star_be.studygroup.entity.StudyGroup;
import com.mercury.star_be.studygroup.repository.GroupMemberRepository;
import com.mercury.star_be.studygroup.repository.StudyGroupRepository;
import com.mercury.star_be.studygroup.service.StudyGroupService;
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
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChatServiceImpl implements ChatService {

    private final ChatMessageRepository chatMessageRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final UserChatRoomRepository userChatRoomRepository;
    private final UserRepository userRepository;
    private final RabbitMessagingTemplate messagingTemplate;
    private final ObjectMapper objectMapper;
    private final ChatMessageFileRepository chatMessageFileRepository;
    private final StudyGroupRepository studyGroupRepository;
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
            .chatMembers(getChatRoomMembers(chatRoom))
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
        List<ChatMessage> chatMessages = chatMessageRepository.findByChatRoomId(chatRoomId).orElse(List.of());

        return chatMessages.stream()
                .map(chatMessage -> ChatRoomMessageDto.builder()
                        .id(chatMessage.getId())
                        .senderId(chatMessage.getChatSender().getId())
                        .nickName(chatMessage.getChatSender().getNickname())
                        .content(chatMessage.getContent())
                        .unreadCount(chatMessage.getUnreadCount())
                        .createdAt(chatMessage.getCreatedAt())
                        .messageFiles(chatMessage.getContent() == null ?
                                (!chatMessage.getChatMessageFiles().isEmpty() ?
                                        chatMessage.getChatMessageFiles().stream()
                                                .map(this::convertToFileDto)
                                                .collect(Collectors.toList())
                                        : null)
                                : null)
                        .build())
                .collect(Collectors.toList());
    }

    private ChatMessageFileDto convertToFileDto(ChatMessageFile chatMessageFile) {
        return ChatMessageFileDto.builder()
                .id(chatMessageFile.getId())
                .fileUrl(chatMessageFile.getFileUrl())
                .fileType(chatMessageFile.getFileType())
                .chatMessageId(chatMessageFile.getChatMessage().getId())
                .build();
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
     * 채팅 전송 서비스
     * 일반 채팅 메시지 / 파일 업로드
     */
    @Override
    public ChatMessageResponse sendMessage(ChatMessageRequest chatMessageRequest) {
        // Redis에 채팅 데이터 저장

        // STOMP 메시지를 받아 RabbitMQ로 메시지 전달
        ChatRoom chatRoom = findByChatRoomId(chatMessageRequest.getChatRoomId());

        // 구독자에게 메시지 전달
        try {
            String messageJson = objectMapper.writeValueAsString(chatMessageRequest);
            messagingTemplate.convertAndSend("/topic/chat." + chatMessageRequest.getChatRoomId(), messageJson);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            throw new BusinessException(ChatErrorCode.CHAT_MESSAGE_CONVERT_ERROR);
        } catch (MessagingException e) {
            e.printStackTrace();
            throw new BusinessException(ChatErrorCode.MESSAGE_SENDING_ERROR);
        }

        // 송신자 조회
        User chatSender = userRepository.findById(chatMessageRequest.getSenderId())
                .orElseThrow(() -> new BusinessException(UserErrorCode.USER_NOT_EXIST));

        // 읽지 않은 메시지 수
        int unreadCount = 1;
        if (chatRoom.getChatRoomType() == ChatRoomType.GROUP) {
            //메시지 송신자 제외
            unreadCount = chatRoom.getStudyGroup().getMemberCount() - 1;
        }

        // 메시지 객체 생성
        ChatMessage chatMessage = ChatMessage.builder()
                .content(chatMessageRequest.getMessageContent())
                .unreadCount(unreadCount)
                .createdAt(LocalDateTime.now())
                .chatSender(chatSender)
                .chatReceiver(null)
                .chatRoom(chatRoom)
                .build();

        // DM일 경우 수신자 추가
        if (chatMessageRequest.getChatRoomType().equals(ChatRoomType.DM)){
            User chatReceiver = userRepository.findById(chatMessageRequest.getReceiverId())
                    .orElseThrow(() -> new BusinessException(UserErrorCode.USER_NOT_EXIST));
            chatMessage.updateReceiver(chatReceiver);
        }
        // 메시지 파일 객체 생성 및 저장
        List<ChatMessageFile> chatMessageFiles = new ArrayList<>();
        if (chatMessageRequest.getMessageFiles() != null && !chatMessageRequest.getMessageFiles().isEmpty()) {
            chatMessageRequest.fileUploadContentString();
            for (ChatMessageFileDto dto : chatMessageRequest.getMessageFiles()) {
                ChatMessageFile chatMessageFile = ChatMessageFile.builder()
                        .fileUrl(dto.getFileUrl())
                        .fileType(dto.getFileType())
                        .chatMessage(chatMessage)
                        .build();
                chatMessageFiles.add(chatMessageFile);
                chatMessageFileRepository.save(chatMessageFile);
            }
        }
        // 파일 업데이트 및 저장
        chatMessage.updateFiles(chatMessageFiles);
        chatMessageRepository.save(chatMessage);
        
        // ChatMessageResponse 생성
        ChatMessageResponse response = ChatMessageResponse.builder()
                .nickName(chatMessageRequest.getNickName())
                .createdAt(LocalDateTime.now())
                .unreadCount(unreadCount) // 읽지 않은 메시지 수 (기본값 : 채팅방 인원)
                .messageContent(chatMessageRequest.getMessageContent())
                .messageFiles(chatMessageRequest.getMessageFiles()) // 파일 정보 추가
                .build();

        return response;
    }


    /**
     * 채팅방 생성 서비스
     * 1:1 채팅방 생성 : sender, receiver 둘 다 저장
     * 그룹채팅방 개설 : sender만 저장
     */
    @Override
    public void createDMChatRoom(CreateChatRoomRequest createChatRoomRequest) {
        //송신자
        User sender =
                userRepository.findById(createChatRoomRequest.getSenderId()).orElseThrow(
                        () -> new RuntimeException("송신자를 찾지 못합니다.")
                );

        //수신자 정보 가져오기
        User receiver =
                userRepository.findById(createChatRoomRequest.getReceiverId()).orElseThrow(
                        () -> new RuntimeException("수신자를 찾지 못합니다.")
                );

        // 채팅방 새로 생성
        ChatRoom chatRoom = ChatRoom.builder()
                .chatRoomType(ChatRoomType.DM)
                .createdAt(LocalDateTime.now())
                .studyGroup(null)
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

        //1:1 채팅일 경우 수신자 정보 저장
        userChatRoomRepository.save(receiverUserChatRoom);

        //사용자 채팅목록 저장
        userChatRoomRepository.save(senderUserChatRoom);

    }
    @Override
    public void createGroupChatRoom(CreateChatRoomRequest createChatRoomRequest) {
        //송신자
        User sender =
                userRepository.findById(createChatRoomRequest.getSenderId()).orElseThrow(
                        () -> new RuntimeException("송신자를 찾지 못합니다.")
                );

        //스터디그룹 가져오기
        StudyGroup studyGroup =
                studyGroupRepository.findById(createChatRoomRequest.getGroupId()).orElseThrow(
                () -> new BusinessException(StudyGroupErrorCode.STUDY_GROUP_NOT_FOUND)
        );

        // 채팅방 새로 생성
        ChatRoom chatRoom = ChatRoom.builder()
                .chatRoomType(ChatRoomType.GROUP)
                .createdAt(LocalDateTime.now())
                .studyGroup(studyGroup)
                .build();

        //채팅방 저장
        chatRoomRepository.save(chatRoom);

        // 사용자 채팅방 생성(송신자)
        UserChatRoom senderUserChatRoom = UserChatRoom.builder()
                .joinedAt(LocalDateTime.now())
                .isBlock(false)
                .chatRoom(chatRoom)
                .chatUser(sender)
                .build();

        //사용자 채팅목록 저장
        userChatRoomRepository.save(senderUserChatRoom);

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
    /**그룹채팅 가입 서비스*/
    @Override
    public ChatRoomJoinResponse joinChatRoom(ChatRoomJoinRequest chatRoomJoinRequest) {

        User joinUser =
                userRepository.findById(chatRoomJoinRequest.getUserId()).orElseThrow(
                        () -> new RuntimeException(String.valueOf(UserErrorCode.USER_NOT_EXIST))
                );
        ChatRoom chatRoom = findByChatRoomId(chatRoomJoinRequest.getChatRoomId());
        UserChatRoom userChatRoom = UserChatRoom.builder()
                .joinedAt(LocalDateTime.now())
                .isBlock(false)
                .chatRoom(chatRoom)
                .chatUser(joinUser)
                .build();
        userChatRoomRepository.save(userChatRoom);

        ChatRoomJoinResponse chatRoomJoinResponse = ChatRoomJoinResponse.builder()
                .result(true)
                .build();
        return chatRoomJoinResponse;
    }
    /**
     * 사용자가 해당 채팅방을 구독중인지 확인하는 서비스
     * */
    @Override
    public boolean isJoinedChatRoom(Long chatUserId, Long chatRoomId) {
        boolean isJoined = userChatRoomRepository.existsByChatUserIdAndChatRoomId(chatUserId, chatRoomId);
        if (!isJoined) {
            throw new BusinessException(ChatErrorCode.USER_CHAT_ROOM_NOT_FOUND);
        }
        return true;
    }

    /**
     * 채팅방 id를 받아 List<ChatRoomMemberDto>로 return 서비스
     */
    @Override
    public List<ChatRoomMemberDto> getChatRoomMembers(ChatRoom chatRoom) {

        List<ChatRoomMemberDto> chatRoomMembers = new ArrayList<>();

        //해당 채팅방에 참여하고 있다는걸 알려주는 user_chat_room가져오기
        if (chatRoom.getChatRoomType().equals(ChatRoomType.DM)) {

            List<UserChatRoom> userChatRooms =
                    userChatRoomRepository.findByChatRoomId(chatRoom.getId())
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
