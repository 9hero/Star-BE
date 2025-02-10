package com.mercury.star_be.timer.service;

import com.mercury.star_be.timer.dto.TimerRankingPeriod;
import com.mercury.star_be.timer.dto.TimerRankingResponseDto;

import java.util.List;

public interface TimerRankingService {
    List<TimerRankingResponseDto> getGroupPeriodStudyTimeRanking(Long groupId, TimerRankingPeriod period);
}
