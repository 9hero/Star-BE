package com.mercury.star_be.global.config;

import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.Arrays;
/**이 클래스는 RabbitMQ로부터 메시지를 수신하는 역할을 해주는 클래스*/
@Component
public class RabbitMQMessageListener {
    //해당 메서드를 RabbitMQ의 특정 큐(chat.queue)에 바인딩
    @RabbitListener(queues = "chat.queue")
    public void receiveMessage(final Message message) {
        System.out.println(Arrays.toString(message.getBody()));
    }
}
