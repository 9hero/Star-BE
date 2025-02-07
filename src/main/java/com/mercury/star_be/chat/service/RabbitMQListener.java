package com.mercury.star_be.chat.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mercury.star_be.chat.dto.request.ChatReadRequest;
import com.mercury.star_be.chat.dto.request.ChatUpdateReadMessagesRequest;
import com.mercury.star_be.chat.dto.response.ChatReadResponse;
import com.mercury.star_be.chat.entity.ChatMessage;
import com.mercury.star_be.chat.entity.ChatRead;
import com.mercury.star_be.chat.repository.ChatMessageRepository;
import com.mercury.star_be.chat.repository.ChatReadRepository;
import com.mercury.star_be.global.error.BusinessException;
import com.mercury.star_be.global.error.code.ChatErrorCode;
import com.mercury.star_be.global.error.code.UserErrorCode;
import com.mercury.star_be.user.entity.User;
import com.mercury.star_be.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.MessagingException;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static com.mercury.star_be.global.config.RabbitMQConfig.READ_CHECK_BULK_RESPONSE_QUEUE_NAME;

@Service
@RequiredArgsConstructor
public class RabbitMQListener {

    private final ChatReadRepository chatReadRepository;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;
    private final SimpMessagingTemplate messagingTemplate;
    private final ChatMessageRepository chatMessageRepository;

    @RabbitListener(queues = READ_CHECK_BULK_RESPONSE_QUEUE_NAME)
    public void sendUpdatedMessageIds(String messageJson){
        ChatUpdateReadMessagesRequest request = null;
        try {
            request = objectMapper.readValue(messageJson, ChatUpdateReadMessagesRequest.class);
            messagingTemplate.convertAndSend("/topic/readCheck.bulkResponse." + request.getChatRoomId(), messageJson);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            throw new BusinessException(ChatErrorCode.CHAT_MESSAGE_CONVERT_ERROR);
        } catch (MessagingException e) {
            e.printStackTrace();
            throw new BusinessException(ChatErrorCode.MESSAGE_SENDING_ERROR);
        }
    }

    @RabbitListener(queues = "readCheck.request.queue")
    @Transactional
    public void processReadCheckMessage(String messageJson) {
        ChatReadRequest chatReadRequest = null;
        try {
            chatReadRequest = objectMapper.readValue(messageJson, ChatReadRequest.class);
            ChatMessage chatMessage = chatMessageRepository.findById(chatReadRequest.getChatMessageId()).orElseThrow(
                    () -> new BusinessException(ChatErrorCode.CHAT_MESSAGE_NOT_FOUND)
            );

            if (!chatReadRepository.existsByChatMessageIdAndChatUserId(chatReadRequest.getChatMessageId(), chatReadRequest.getChatUserId())) {
                if (chatMessage.getUnreadCount() > 0) {
                    chatMessage.updateUnreadCount(chatMessage.getUnreadCount() - 1);
                }

                User chatUser = userRepository.findById(chatReadRequest.getChatUserId()).orElseThrow(() -> new BusinessException(UserErrorCode.USER_NOT_EXIST));
                ChatRead chatRead = ChatRead.builder()
                        .chatUser(chatUser)
                        .chatMessage(chatMessage)
                        .createdAt(LocalDateTime.now())
                        .build();

                chatReadRepository.save(chatRead);

                ChatReadResponse chatReadResponse = ChatReadResponse.builder()
                        .chatMessageId(chatMessage.getId())
                        .unreadCount(chatMessage.getUnreadCount())
                        .build();

                try {
                    String readMessageJson = objectMapper.writeValueAsString(chatReadResponse);
                    messagingTemplate.convertAndSend("/topic/readCheck.response." + chatMessage.getChatRoom().getId(), readMessageJson);
                } catch (JsonProcessingException e) {
                    e.printStackTrace();
                    throw new BusinessException(ChatErrorCode.CHAT_MESSAGE_CONVERT_ERROR);
                } catch (MessagingException e) {
                    e.printStackTrace();
                    throw new BusinessException(ChatErrorCode.MESSAGE_SENDING_ERROR);
                }
            } else {
                throw new BusinessException(ChatErrorCode.CHAT_ALREADY_READ);
            }
        } catch (BusinessException e) {
            // 예외 처리: 이미 읽음 표시된 메시지에 대해 로깅
            if (chatReadRequest != null) {
                System.err.println("이미 읽음 표시된 메시지입니다. 메시지 ID: " + chatReadRequest.getChatMessageId() + ", 사용자 ID: " + chatReadRequest.getChatUserId());
            }
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            throw new BusinessException(ChatErrorCode.CHAT_MESSAGE_CONVERT_ERROR);
        } catch (RuntimeException e) {
            e.printStackTrace();
        }
    }

}
