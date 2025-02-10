package com.mercury.star_be.timer.controller;

import com.mercury.star_be.global.common.ApiResponse;
import com.mercury.star_be.timer.dto.TimerRankingPeriod;
import com.mercury.star_be.timer.dto.TimerRankingResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/timers/ranking")
@RequiredArgsConstructor
public class TimerRankingController {

    private final TimerRankingServiceImpl timerRankingServiceImpl;

    /**
     집중방 랭킹 조회
      */
    @GetMapping("/groups/{groupId}")
    public ApiResponse<List<TimerRankingResponseDto>> getGroupPeriodStudyTimeRanking(@PathVariable Long groupId,
                                                               @RequestParam TimerRankingPeriod period) {
        List<TimerRankingResponseDto> result =  timerRankingServiceImpl.getGroupPeriodStudyTimeRanking(groupId, period);
        return ApiResponse.success(result);
    }
}
