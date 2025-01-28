package com.mercury.star_be.studygroup.dto.response;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
public class NoticeResponse {

	private Long id;
	private String title;
	private String content;
	private String writer;
	private LocalDateTime createdAt;
}
