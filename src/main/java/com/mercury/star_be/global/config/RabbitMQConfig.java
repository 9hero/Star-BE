package com.mercury.star_be.global.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**Spring Boot 애플리케이션에서 RabbitMQ와의 통합 설정*/
@Configuration
public class RabbitMQConfig {
    @Value("${spring.rabbitmq.host}")
    String rabbitmqHost;
    @Value("${spring.rabbitmq.password}")
    String rabbitmqPassword;
    @Value("${spring.rabbitmq.username}")
    String rabbitmqUsername;
    @Value("${spring.rabbitmq.port}")
    int rabbitmqPort;
    @Bean
    public Queue chatQueue() {
        //chat.queue라는 이름의 새로운 큐를 생성
        return new Queue("chat.queue");
    }

    @Bean
    public TopicExchange chatExchange() {
        //chat.exchange라는 이름의 Topic Exchange를 생성하고 반환
        return new TopicExchange("chat.exchange");
    }

    //chat.queue와 chat.exchange 사이의 바인딩을 생성하고 반환
    @Bean
    public Binding binding(Queue chatQueue, TopicExchange chatExchange) {
        //메시지가 chat.routing.key라는 라우팅 키를 사용하여 chat.exchange로 전송될 때 chat.queue로 라우팅됩니다.
        return BindingBuilder.bind(chatQueue).to(chatExchange).with("chat.routing.key");
    }

    @Bean
    public CachingConnectionFactory connectionFactory() {
        CachingConnectionFactory connectionFactory = new CachingConnectionFactory(rabbitmqHost);
        connectionFactory.setPort(rabbitmqPort);
        connectionFactory.setUsername(rabbitmqUsername);  // 여기에 사용자 이름을 입력하세요
        connectionFactory.setPassword(rabbitmqPassword);  // 여기에 비밀번호를 입력하세요
        return connectionFactory;
    }

    @Bean
    public RabbitTemplate rabbitTemplate(CachingConnectionFactory connectionFactory) {
        return new RabbitTemplate(connectionFactory);
    }
}
