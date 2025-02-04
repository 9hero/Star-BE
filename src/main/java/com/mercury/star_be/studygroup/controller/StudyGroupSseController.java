package com.mercury.star_be.studygroup.controller;

import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.mercury.star_be.studygroup.service.StudyGroupSseService;
import com.mercury.star_be.user.util.JwtUtil;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class StudyGroupSseController {

	private final StudyGroupSseService studyGroupSseService;
	private final JwtUtil jwtUtil;

	@GetMapping(value = "/api/groups/{groupId}/subscribe", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
	public SseEmitter subscribe(@PathVariable(value = "groupId") Long groupId, Authentication auth) {
		Long userId = jwtUtil.getAuthenticatedUser(auth).getId();
		return studyGroupSseService.subscribe(groupId, userId);
	}
}
