package com.mercury.star_be.timer.service;

import com.mercury.star_be.studygroup.entity.StudyGroup;
import com.mercury.star_be.studygroup.repository.StudyGroupRepository;
import com.mercury.star_be.timer.dto.TimerDto;
import com.mercury.star_be.timer.dto.TimerEvent;
import com.mercury.star_be.timer.entity.Timer;
import com.mercury.star_be.timer.enums.TimerStatus;
import com.mercury.star_be.timer.repository.TimerRepository;
import com.mercury.star_be.user.entity.User;
import com.mercury.star_be.user.repository.UserRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SetOperations;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class TimerServiceImpl implements TimerService {

    private final TimerRepository timerRepository;
    private final RedisTemplate<String, Object> redisTemplate;
    private final UserRepository userRepository;
    private final StudyGroupRepository studyGroupRepository;
    // 집중방에 실시간으로 연결 된 사용자 정보를 가져옴 - Redis로 id 조회, db로 데이터를 가져옴
    public Set<TimerDto> getFocusRoomTimerDataByGroupId(Long groupId) {

        // 응답값 초기화
        Set<TimerDto> timerData = new HashSet<>(); // TimerDto 중복 예방
        Map<Long,String> focusRoomMemberInfo = getFocusRoomMemberIdsFrom(groupId);

        // 집중방 접속 인원 없음.
        if (focusRoomMemberInfo == null) {
            return timerData;
        }
        List<Long> memberIds = new ArrayList<>(focusRoomMemberInfo.keySet());

        // Timer groupId & userIds로 조회, User도 같이 조회
        List<Timer> memberTimers = timerRepository.findTimersWithUsersBygidAnduid(groupId, memberIds);

        // 타이머가 있는 접속 유저들
        Map<Long, Timer> currentMembersTimers = memberTimers.stream()
                .collect(Collectors.toMap(Timer::getUserId, timer -> timer));

        // 없는 유저들 분리
        for (Map.Entry<Long,String> entry : focusRoomMemberInfo.entrySet()) {
            Long memberId = entry.getKey();
            // 타이머가 없는 경우
            if (!currentMembersTimers.containsKey(memberId)) {
                TimerDto entryEvent = TimerDto.builder()
                        .event(TimerEvent.ENTRY)
                        .userId(memberId)
                        .nickname(entry.getValue())
                        .status("rest")
                        .todayTotalTime(0)
                        .ranking(0)
                        .timeSoFar(0)
                        .build();
                timerData.add(entryEvent);
            }
            // 타이머가 있는 유저
            else {
                // Entity -> Dto
                timerData.add(new TimerDto(currentMembersTimers.get(memberId)));
            }
        }
        return timerData;
    }

    // 집중방 id로 Redis에 저장된 참여중인 유저id & nickname 가져오기
    private Map<Long,String> getFocusRoomMemberIdsFrom(Long groupId) {
        // userId, nickname
        Map<Long,String> result = new HashMap<>();

        // Redis에서 데이터 id 가져오기
        String focusRoomKey = "focus:" + groupId;
        SetOperations<String, Object> setOps = redisTemplate.opsForSet();
        Set<Object> memberIdAndNicknameSet = setOps.members(focusRoomKey);
        if (memberIdAndNicknameSet == null) {
            return result;
        }
        // cache Object -> String -> Map<Long,String> 변환 "userId:nickname"
        memberIdAndNicknameSet.forEach(idAndNickname -> {
                    String[] split = idAndNickname.toString().split(":");
                    result.put(Long.parseLong(split[0]),split[1]);
                }
        );
        return result;
    }

    /**
     * 타이머 일시 정지.
     * @param groupId
     * @param userId
     * @return TimerDto 일시 정지된 타이머 정보 반환 혹은 오늘자 새 타이머 반환. (타이머가 없는 경우 null 반환)
     */
    @Transactional
    public TimerDto stopTimerByGroupIdAndUserId(Long groupId, Long userId) {
        // 최신 타이머 조회
        Timer timer = timerRepository.findRecentOneByStudyGroupIdAndUserId(groupId,userId);

        // 날짜 비교
        if (timer != null) {
            // 오늘 시작한 타이머인 경우
            boolean isToday = timer.getStudyDate().equals(LocalDate.now());
            if (isToday) {
                // 타이머 중지
                timer.stop();
                Timer savedTimer = timerRepository.save(timer);

                return savedTimer.toEventTimerDto(TimerEvent.STOP);
            }
            // 타이머가 오늘 시작한 것이 아닌 경우 예) 오후 11시 시작 -> 12시 넘어서 종료
            else {
                // 자정 (공부 시작 일의 23:59:59 생성)
                LocalDateTime endOfDay = timer.getStudyDate().atTime(23, 59, 59);

                // 어제의 타이머 일시정지 없이 종료처리
                timer.end();
                Timer savedFormerTimer = timerRepository.save(timer);

                // 자정 이후의 새로운 타이머 생성
                Timer todayNewTimer = Timer.builder()
                    .user(timer.getUser())
                    .studyDate(LocalDate.now())
                    .studyGroup(timer.getStudyGroup())
                    .build();
                todayNewTimer.stop();
                todayNewTimer.setExceedTimeSoFarAfterMidnight();
                Timer savedTodayTimer = timerRepository.save(todayNewTimer);

                return savedTodayTimer.toEventTimerDto(TimerEvent.STOP);
            }
        }
        // Timer 조회 실패. null 일 경우
        else {
            log.info("Timer not found");
            return null;
        }
    }

    /**
     * 집중방 id와 사용자 id로 오늘의 타이머 정보 가져오기
     * @param groupId
     * @param UserId
     * @return
     */
    @Override
    @Transactional
    public TimerDto startMyTimer(long groupId, long UserId) {
        // [오늘] 시작한 타이머가 있는 경우 (타이머 재개 & n번 째 공부 시작)
        Timer timer = timerRepository.findByStudyGroupIdAndUserIdAndToday(groupId, UserId);
        if (timer != null) {
            System.out.println(" 오늘 공부 재시작! ");
            // 타이머 시작
            timer.start();
            Timer savedTimer = timerRepository.save(timer);

            return savedTimer.toEventTimerDto(TimerEvent.START);
        }

        System.out.println(" 오늘의 첫 공부 시작! ");
        // 타이머가 없는 경우 (새로운 타이머 시작)
        // 연관관계 가져오기.
        User user= userRepository.findById(UserId).get();
        StudyGroup studyGroup = studyGroupRepository.findById(groupId).get();

        // Entity 생성
        Timer newTimer = Timer.builder()
                .user(user)
                .studyDate(LocalDate.now())
                .studyGroup(studyGroup)
                .build();

        newTimer.start();

        // 저장
        Timer savedTimer = timerRepository.save(newTimer);

        // Dto로 변환
        TimerDto event = savedTimer.toEventTimerDto(TimerEvent.START);
        event.setStatus(TimerStatus.START.toString());
        return event;
    }

    @Override
    public TimerDto endTimerByGroupIdAndUserId(Long groupId, Long userId) {
        // 최신 타이머 조회
        Timer timer = timerRepository.findRecentOneByStudyGroupIdAndUserId(groupId,userId);
        if (timer == null) {
            log.info("Timer not found");
            return null;
        }else {
            // 타이머 종료
            timer.end();
            Timer savedTimer = timerRepository.save(timer);

            return savedTimer.toEventTimerDto(TimerEvent.END);
        }
    }
}
