package com.mercury.star_be.timer.dto;

import com.mercury.star_be.timer.entity.Timer;
import lombok.*;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode
public class TimerDto{

    private TimerEvent event;
    private Long timerId;
    private Long userId;
    private String nickname;
    private long timeSoFar;
    private long todayTotalTime;
    private int ranking;
    private String status;

    public TimerDto(Timer timer) {
        this.timerId = timer.getId();
        this.userId = timer.getUserId();
        this.nickname = timer.getUser().getNickname();
        this.timeSoFar = timer.getTimeSoFar();
        this.todayTotalTime = timer.getTotalTime();
        this.status = timer.getStatus().toString();
    }
}
