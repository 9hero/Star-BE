package com.mercury.star_be.studygroup.controller;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.mercury.star_be.studygroup.service.StudyGroupSseService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class StudyGroupSseController {

	private final StudyGroupSseService studyGroupSseService;

	@GetMapping(value = "/api/groups/{groupId}/subscribe", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
	public SseEmitter subscribe(@PathVariable(value = "groupId") Long groupId, @RequestParam(value = "userId") Long userId) {
		// TODO: access 토큰의 userId로 변경
		return studyGroupSseService.subscribe(groupId, userId);
	}
}
