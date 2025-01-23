package com.mercury.star_be.studygroup.service;

import java.io.IOException;
import java.util.List;
import java.util.Set;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.mercury.star_be.studygroup.dto.response.GroupMemberSseResponse;
import com.mercury.star_be.studygroup.dto.response.MemberStatusSseResponse;
import com.mercury.star_be.studygroup.entity.ConnectionStatus;
import com.mercury.star_be.studygroup.entity.GroupMember;
import com.mercury.star_be.studygroup.repository.GroupMemberRepository;
import com.mercury.star_be.studygroup.repository.SseEmitterRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class StudyGroupSseServiceImpl implements StudyGroupSseService {

	private final RedisTemplate<String, Object> redisTemplate;
	private final SseEmitterRepository sseEmitterRepository;
	private final GroupMemberRepository groupMemberRepository;

	private static final Long SSE_TIMEOUT = 60 * 60 * 1000L;	// 1시간

	@Override
	public SseEmitter subscribe(Long groupId, Long userId) {
		SseEmitter sseEmitter = createSseEmitter(groupId, userId);
		// SSE 연결 시 전체 그룹원 정보 send
		sendGroupMemberInfoList(groupId, sseEmitter);

		// 접속으로 변경된 정보 연결된 SSE에 send
		MemberStatusSseResponse memberStatusSseResponse = MemberStatusSseResponse.builder()
			.userId(userId)
			.status(ConnectionStatus.ONLINE)
			.build();
		sendToGroup(groupId, memberStatusSseResponse);
		return sseEmitter;
	}

	private SseEmitter createSseEmitter(Long groupId, Long userId) {
		SseEmitter sseEmitter = new SseEmitter(SSE_TIMEOUT);
		sseEmitterRepository.save(groupId, userId, sseEmitter);

		// 요청이 완료되거나 타임아웃 발생 시 기존 SseEmitter 삭제
		sseEmitter.onCompletion(() -> disconnect(groupId, userId, sseEmitter));
		sseEmitter.onTimeout(() -> disconnect(groupId, userId, sseEmitter));
		return sseEmitter;
	}

	private void disconnect(Long groupId, Long userId, SseEmitter sseEmitter) {
		sseEmitterRepository.delete(groupId, userId, sseEmitter);

		MemberStatusSseResponse memberStatusSseResponse = MemberStatusSseResponse.builder()
			.userId(userId)
			.status(ConnectionStatus.OFFLINE)
			.build();
		sendToGroup(groupId, memberStatusSseResponse);
	}

	private void sendGroupMemberInfoList(Long groupId, SseEmitter sseEmitter) {
		List<GroupMemberSseResponse> groupMemberInfoList = getGroupMemberInfoList(groupId);
		try {
			sseEmitter.send(SseEmitter.event()
				.name("memberData")
				.data(groupMemberInfoList));
		} catch (IOException e) {
			sseEmitter.completeWithError(e);
		}
	}

	private List<GroupMemberSseResponse> getGroupMemberInfoList(Long groupId) {
		List<GroupMember> groupMembers = groupMemberRepository.findByGroupIdOrderByNicknameAsc(groupId);
		List<Long> connectedUserIds = sseEmitterRepository.getConnectedUsers(groupId);

		return groupMembers.stream()
			.map(groupMember -> GroupMemberSseResponse.builder()
				.id(groupMember.getId())
				.nickname(groupMember.getNickname())
				.image(groupMember.getImage())
				.isHost(groupMember.isHost())
				.status(
					connectedUserIds.contains(groupMember.getId()) ? ConnectionStatus.ONLINE : ConnectionStatus.OFFLINE)
				.build())
			.toList();
	}

	private void sendToGroup(Long groupId, Object data) {
		Set<SseEmitter> sseEmitters = sseEmitterRepository.findAllByGroupId(groupId);
		sseEmitters.forEach(sseEmitter -> {
			if (sseEmitter != null) {
				try {
					sseEmitter.send(SseEmitter.event()
						.name("statusUpdate")
						.data(data));
				} catch (IOException e) {
					sseEmitter.completeWithError(e);
				}
			}
		});
	}
}
