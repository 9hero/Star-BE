package com.mercury.star_be.timer.dto;

public record TimerDto(    Long userId,
                           String nickname,
                           int timeSoFar,
                           int ranking,
                           int todayTotalTime,
                           String status) {

}
