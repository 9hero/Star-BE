package com.mercury.star_be.timer.service;

import com.mercury.star_be.global.error.BusinessException;
import com.mercury.star_be.global.error.code.TimerErrorCode;
import com.mercury.star_be.timer.dto.TimerDto;
import com.mercury.star_be.timer.dto.TimerEvent;
import com.mercury.star_be.timer.entity.Timer;
import com.mercury.star_be.timer.repository.TimerRepository;
import com.mercury.star_be.user.entity.User;
import jakarta.annotation.PostConstruct;

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

    // 집중방에 실시간으로 연결 된 사용자 정보를 가져옴 - Redis로 id 조회, db로 데이터를 가져옴
    public List<TimerDto> getFocusRoomTimerDataByGroupId(Long groupId) {

        // 응답값 초기화
        List<TimerDto> timerData = new ArrayList<>();
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

    @Transactional
    public void stopTimerByGroupIdAndUserId(Long groupId, Long userId) {
        // 타이머 중지
        Timer timer = timerRepository.findByStudyGroupIdAndUserId(groupId,userId);
        if (timer != null) {
            timer.stop();
            timerRepository.save(timer);
            System.out.println("Timer stopped");
        } else {
            log.info("Timer not found");
        }
    }

}
