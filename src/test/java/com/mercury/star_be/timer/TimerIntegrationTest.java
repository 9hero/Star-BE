
package com.mercury.star_be.timer;

import com.mercury.star_be.common.DatabaseCleanup;
import com.mercury.star_be.file.service.GcsFileServiceImpl;
import com.mercury.star_be.fixture.StudyGroupFixture;
import com.mercury.star_be.fixture.UserFixture;
import com.mercury.star_be.global.config.WebSocketBrokerConfiguration;
import com.mercury.star_be.studygroup.entity.GroupMember;
import com.mercury.star_be.studygroup.entity.StudyGroup;
import com.mercury.star_be.studygroup.repository.GroupMemberRepository;
import com.mercury.star_be.studygroup.repository.StudyGroupRepository;
import com.mercury.star_be.timer.dto.TimerDto;
import com.mercury.star_be.timer.dto.TimerEvent;
import com.mercury.star_be.timer.entity.Timer;
import com.mercury.star_be.timer.enums.TimerStatus;
import com.mercury.star_be.timer.repository.TimerRepository;
import com.mercury.star_be.user.entity.User;
import com.mercury.star_be.user.repository.UserRepository;
import com.mercury.star_be.user.util.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.simp.stomp.StompFrameHandler;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;
import org.springframework.web.socket.WebSocketHttpHeaders;
import org.springframework.web.socket.client.WebSocketClient;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;

import java.lang.reflect.Type;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(WebSocketBrokerConfiguration.class)
public class TimerIntegrationTest {

    private static final Logger log = LoggerFactory.getLogger(TimerIntegrationTest.class);
    @MockBean
    private GcsFileServiceImpl gcsFileService;

    @LocalServerPort
    private int port;

    @Autowired
    private DatabaseCleanup databaseCleanup;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private StudyGroupRepository studyGroupRepository;
    @Autowired
    private GroupMemberRepository groupMemberRepository;
    @Autowired
    private TimerRepository timerRepository;
    @Autowired
    private JwtUtil jwtUtil;
    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    private WebSocketStompClient stompClient;
    private String url;

    @BeforeEach
    void setUp() {
        databaseCleanup.execute();
        WebSocketClient webSocketClient = new StandardWebSocketClient();
        this.stompClient = new WebSocketStompClient(webSocketClient);
        this.stompClient.setMessageConverter(new MappingJackson2MessageConverter());
        this.url = String.format("ws://localhost:%d/timer", port);
    }

    @Test
    @DisplayName("웹소켓 연결이 끊어지면 사용자의 타이머가 자동으로 중지된다.")
    void webSocketDisconnect_stopsTimer() throws Exception {
        // given
        User user = userRepository.save(UserFixture.createUser("T1"));
        StudyGroup studyGroup = studyGroupRepository.save(StudyGroupFixture.createStudyGroup("S1", 10));
        GroupMember groupMember = groupMemberRepository.save(GroupMember.builder().member(user).group(studyGroup).nickname(user.getNickname()).isHost(true).joinedAt(LocalDateTime.now()).build());
        Long groupMemberId = groupMember.getId();

        Timer newTimer = Timer.builder().groupMember(groupMember).studyDate(LocalDate.now()).build();
        newTimer.start();
        Timer savedTimer = timerRepository.save(newTimer);

        StompHeaders connectHeaders = new StompHeaders();
        connectHeaders.add("roomType", "focus");
        connectHeaders.add("groupId", studyGroup.getId().toString());
        connectHeaders.add("groupMemberId", groupMemberId.toString());
        connectHeaders.add("userId", user.getId().toString());
        connectHeaders.add("nickname", user.getNickname());

        CompletableFuture<StompSession> sessionFuture = stompClient.connectAsync(url, new WebSocketHttpHeaders(), connectHeaders, new StompSessionHandlerAdapter() {});
        StompSession stompSession = sessionFuture.get(5, TimeUnit.SECONDS);

        // when
        stompSession.disconnect();

        // then
        Thread.sleep(1000);
        Timer timer = timerRepository.findById(savedTimer.getId()).get();
        assertThat(timer.getStatus()).isEqualTo(TimerStatus.STOP);
        log.info("is timer status stop? :{}", timer.getStatus().toString());
        timerRepository.delete(timer);
    }

    @Test
    @DisplayName("웹소켓 연결 해제 시, 구독자에게 DISCONNECT 이벤트가 전송된다.")
    void webSocketDisconnect_Should_Send_DisconnectEvent() throws Exception {
        // given
        User user = userRepository.save(UserFixture.createUser("T2"));
        StudyGroup studyGroup = studyGroupRepository.save(StudyGroupFixture.createStudyGroup("S2", 10));
        GroupMember groupMember = groupMemberRepository.save(GroupMember.builder().member(user).group(studyGroup).nickname(user.getNickname()).isHost(true).joinedAt(LocalDateTime.now()).build());
        Long groupId = studyGroup.getId();
        Long userId = user.getId();
        Long groupMemberId = groupMember.getId();
        String nickname = user.getNickname();

        // FIX: 2개의 메시지(ENTRY, DISCONNECT)를 받을 수 있도록 큐 크기 변경
        BlockingQueue<TimerDto> messageQueue = new java.util.concurrent.ArrayBlockingQueue<>(2);

        // 1. 메시지를 구독할 클라이언트 설정
        WebSocketStompClient subscriberClient = new WebSocketStompClient(new StandardWebSocketClient());
        subscriberClient.setMessageConverter(new MappingJackson2MessageConverter());
        StompSession subscriberSession = subscriberClient.connectAsync(url, new StompSessionHandlerAdapter() {}).get(5, TimeUnit.SECONDS);
        subscriberSession.subscribe("/topic/groups." + groupId + ".timers", new StompFrameHandler() {
            @Override
            public Type getPayloadType(StompHeaders headers) {
                return TimerDto.class;
            }
            @Override
            public void handleFrame(StompHeaders headers, Object payload) {
                messageQueue.add((TimerDto) payload);
            }
        });

        // 2. 이벤트를 발생시킬 클라이언트가 연결 (이 때 ENTRY 이벤트 발생)
        WebSocketStompClient eventTriggerClient = new WebSocketStompClient(new StandardWebSocketClient());
        eventTriggerClient.setMessageConverter(new MappingJackson2MessageConverter());
        StompHeaders connectHeaders = new StompHeaders();
        connectHeaders.add("roomType", "focus");
        connectHeaders.add("groupId", groupId.toString());
        connectHeaders.add("groupMemberId", groupMemberId.toString());
        connectHeaders.add("userId", userId.toString());
        connectHeaders.add("nickname", nickname);
        StompSession eventTriggerSession = eventTriggerClient.connectAsync(url, new WebSocketHttpHeaders(), connectHeaders, new StompSessionHandlerAdapter() {}).get(5, TimeUnit.SECONDS);

        // when
        eventTriggerSession.disconnect(); // DISCONNECT 이벤트 발생

        // then
        // FIX: 첫 번째 ENTRY 메시지는 확인 후 버리고, 두 번째 DISCONNECT 메시지를 확인
        messageQueue.poll(2, TimeUnit.SECONDS); // ENTRY 이벤트 소비
        TimerDto receivedMessage = messageQueue.poll(5, TimeUnit.SECONDS); // DISCONNECT 이벤트 확인

        assertThat(receivedMessage).isNotNull();
        assertThat(receivedMessage.getEvent()).isEqualTo(TimerEvent.DISCONNECT);
        assertThat(receivedMessage.getUserId()).isEqualTo(userId);
    }
}
