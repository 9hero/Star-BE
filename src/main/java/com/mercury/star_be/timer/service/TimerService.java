package com.mercury.star_be.timer.service;

import com.mercury.star_be.timer.dto.TimerDto;
import jakarta.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TimerService {

    private final List<TimerDto> tempGroupTimerData = new ArrayList<>();

    // 초기 데이터 추가
    @PostConstruct
    private void initData() {
        tempGroupTimerData.add(new TimerDto(1L, "김 한", 1523, 2, 900, "stop"));
        tempGroupTimerData.add(new TimerDto(2L, "강철수", 4752, 1, 3212, "stop"));
        tempGroupTimerData.add(new TimerDto(3L, "이영희", 1234, 3, 1234, "stop"));
        tempGroupTimerData.add(new TimerDto(4L, "박철수", 1234, 3, 1234, "stop"));
        tempGroupTimerData.add(new TimerDto(5L, "김영희", 12344, 3, 12234, "stop"));
    }

    public List<TimerDto> getFocusRoomTimerDataByGroupId(Long groupId) {
        return tempGroupTimerData;
    }
}
