package com.mercury.star_be.studygroup.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
@Builder
public class NoticeCreateResponse {
    private Long id;
    private String title;
    private String content;
    private String writer;
    private LocalDateTime createdAt;
}
