package com.mercury.star_be.studygroup.service;

import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

public interface StudyGroupSseService {

	SseEmitter subscribe(Long groupId, Long userId);

	void sendFocusRoomMemberCount(Long groupId, int memberCount);
}
