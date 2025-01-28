package com.mercury.star_be.global.config;

import com.mercury.star_be.chat.entity.ChatMessage;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class RabbitMQListener {

    @RabbitListener(queues = "chat.queue")
    public void receiveMessage(final ChatMessage chatMessage) {
        // 메시지 처리 로직
        System.out.println("Received message: " + chatMessage.getContent());
    }
}
