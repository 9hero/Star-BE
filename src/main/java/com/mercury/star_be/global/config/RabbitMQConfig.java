package com.mercury.star_be.global.config;

import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
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

    private static final String CHAT_QUEUE_NAME = "chat.queue";
    private static final String CHAT_EXCHANGE_NAME = "chat.exchange";
    private static final String ROUTING_KEY = "chat.*";
    private static final String READ_CHECK_REQUEST_QUEUE_NAME = "readCheck.request.queue";
    private static final String READ_CHECK_RESPONSE_QUEUE_NAME = "readCheck.response.queue";
    private static final String READ_CHECK_REQUEST_ROUTING_KEY = "readCheck.request.*";
    private static final String READ_CHECK_RESPONSE_ROUTING_KEY = "readCheck.response.*";

    // TIMER QUEUE
    private static final String TIMER_QUEUE_NAME = "groups.queue";
    private static final String TIMER_EXCHANGE_NAME = "groups.exchange";
//    private static final String TIMER_ROUTING_KEY = "groups.*";
    private static final String TIMER_ROUTING_KEY = "groups.#";

    //Queue 등록(채팅 / 메시지 읽음)
    @Bean
    public Queue queue(){ return new Queue(CHAT_QUEUE_NAME, true); }
    @Bean
    public Queue readCheckRequestQueue(){ return new Queue(READ_CHECK_REQUEST_QUEUE_NAME, true); }
    @Bean
    public Queue readCheckResponseQueue(){ return new Queue(READ_CHECK_RESPONSE_QUEUE_NAME, true); }

    // Timer Queue 등록
    @Bean
    public Queue timerQueue() {
        return new Queue(TIMER_QUEUE_NAME,true);
    }

    //Exchange 등록
    @Bean
    public TopicExchange exchange(){ return new TopicExchange(CHAT_EXCHANGE_NAME); }

    // Timer Exchange 등록
    @Bean
    public TopicExchange timerExchange() {
        return new TopicExchange(TIMER_EXCHANGE_NAME);
    }

    //Exchange와 Queue 바인딩
    @Bean
    public Binding binding(Queue queue, TopicExchange exchange) {
        return BindingBuilder.bind(queue).to(exchange).with(ROUTING_KEY);
    }
    @Bean
    public Binding readCheckRequestBinding(Queue readCheckRequestQueue, TopicExchange exchange) {
        return BindingBuilder.bind(readCheckRequestQueue).to(exchange).with(READ_CHECK_REQUEST_ROUTING_KEY);
    }
    @Bean
    public Binding readCheckResponseBinding(Queue readCheckResponseQueue, TopicExchange exchange) {
        return BindingBuilder.bind(readCheckResponseQueue).to(exchange).with(READ_CHECK_RESPONSE_ROUTING_KEY);
    }
    // Timer Exchange와 Queue 바인딩
    @Bean
    public Binding timerBinding(Queue timerQueue, TopicExchange timerExchange) {
        return BindingBuilder.bind(timerQueue).to(timerExchange).with(TIMER_ROUTING_KEY);
    }

    /* messageConverter를 커스터마이징 하기 위해 Bean 새로 등록 */
    @Bean
    public RabbitTemplate rabbitTemplate(){
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory());
        rabbitTemplate.setMessageConverter(jsonMessageConverter());
        return rabbitTemplate;
    }

    //Spring에서 자동생성해주는 ConnectionFactory는 SimpleConnectionFactory인가? 그건데
    //여기서 사용하는 건 CachingConnectionFacotry라 새로 등록해줌
    @Bean
    public ConnectionFactory connectionFactory(){
        CachingConnectionFactory factory = new CachingConnectionFactory();
        factory.setHost(rabbitmqHost);
        factory.setUsername(rabbitmqUsername);
        factory.setPassword(rabbitmqPassword);
        return factory;
    }

    @Bean
    public Jackson2JsonMessageConverter jsonMessageConverter(){
        //LocalDateTime serializable을 위해
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, true);
        objectMapper.registerModule(dateTimeModule());

        Jackson2JsonMessageConverter converter = new Jackson2JsonMessageConverter(objectMapper);

        return converter;
    }

    @Bean
    public Module dateTimeModule(){
        return new JavaTimeModule();
    }
}
