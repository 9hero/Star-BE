package com.mercury.star_be.timer.entity;

import com.mercury.star_be.studygroup.entity.StudyGroup;
import com.mercury.star_be.timer.enums.TimerStatus;
import com.mercury.star_be.user.entity.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import jakarta.persistence.Id;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor
public class Timer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private TimerStatus status;

    private LocalDateTime startTime;

    private Long timeSoFar;

    private LocalDateTime endTime;

    private Long totalTime;

    private LocalDate studyDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "study_group_id")
    private StudyGroup studyGroup;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    public void start() {
        this.status = TimerStatus.START;
        this.startTime = LocalDateTime.now();
    }

    public void stop() {
        if (this.status != TimerStatus.START) {
            return;
        }
        this.status = TimerStatus.STOP;
        // 시작 시점부터 현재까지 공부한 시간 계산
        long elapsedSeconds = Duration.between(startTime, LocalDateTime.now()).getSeconds();
        this.timeSoFar += elapsedSeconds;
        startTime = null;
        // End 안하면 totalTime 유실되기 때문에 추가
        totalTime += timeSoFar;
    }

    public void end(){
        this.status = TimerStatus.END;
        this.endTime = LocalDateTime.now();
        // 시작 시점부터 현재까지 공부한 시간 계산
        long elapsedSeconds = Duration.between(startTime, endTime).getSeconds();
        this.totalTime += elapsedSeconds;
        // 종료 처리
        startTime = null;
        timeSoFar = 0L;
    }

    public Long getUserId() {
        return user.getId();
    }
}
