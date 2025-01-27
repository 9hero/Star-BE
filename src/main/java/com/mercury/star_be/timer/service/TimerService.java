package com.mercury.star_be.timer.service;

import com.mercury.star_be.timer.dto.TimerDto;

import java.util.List;

public interface TimerService {
    List<TimerDto> getFocusRoomTimerDataByGroupId(Long groupId);
    void stopTimerByGroupIdAndUserId(Long groupId,Long userId);
}
