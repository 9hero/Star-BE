package com.mercury.star_be.timer.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum TimerEvent {
    START,
    STOP,
    END,
    DISCONNECT,
    ENTRY;
}
