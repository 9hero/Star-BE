package com.mercury.star_be.studygroup.service;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.mercury.star_be.studygroup.dto.GroupMemberDto;
import com.mercury.star_be.studygroup.dto.response.GroupMemberSseResponse;
import com.mercury.star_be.studygroup.dto.response.MemberStatusSseResponse;
import com.mercury.star_be.studygroup.entity.ConnectionStatus;
import com.mercury.star_be.studygroup.repository.GroupMemberRepository;
import com.mercury.star_be.studygroup.repository.SseEmitterRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class StudyGroupSseServiceImpl implements StudyGroupSseService {

	private final SseEmitterRepository sseEmitterRepository;
	private final GroupMemberRepository groupMemberRepository;

	private static final Long SSE_TIMEOUT = 60 * 60 * 1000L;	// 1시간
	private static final String CONNECT_EVENT = "connect";
	private static final String STATUS_UPDATE_EVENT = "statusUpdate";
	private static final String MEMBER_DATA_EVENT = "memberData";
	private static final String FOCUS_ROOM_MEMBER_COUNT_EVENT = "focusRoomMemberCount";

	@Override
	public SseEmitter subscribe(Long groupId, Long userId) {
		SseEmitter sseEmitter = createSseEmitter(groupId, userId);

		// SSE 연결 시 전체 그룹원 정보 send
		sendGroupMemberInfoList(groupId, sseEmitter);

		// 접속으로 변경된 상태를 그룹에 연결된 SSE에 send
		sendMemberStatusToGroup(groupId, userId, ConnectionStatus.ONLINE);

		// 집중방 인원 수 send
		sendFocusRoomMemberCount(groupId, sseEmitter);
		return sseEmitter;
	}

	private SseEmitter createSseEmitter(Long groupId, Long userId) {
		SseEmitter sseEmitter = new SseEmitter(SSE_TIMEOUT);
		sseEmitterRepository.save(groupId, userId, sseEmitter);
		sseEmitterRepository.updateStatus(groupId, userId, ConnectionStatus.ONLINE);

		// 요청이 완료되거나 타임아웃 발생 시 기존 SseEmitter 삭제
		sseEmitter.onCompletion(() -> disconnect(groupId, userId, sseEmitter));
		sseEmitter.onTimeout(() -> disconnect(groupId, userId, sseEmitter));
		sseEmitter.onError(e -> disconnect(groupId, userId, sseEmitter));

		// 연결 성공 응답
		sendData(sseEmitter, CONNECT_EVENT, true);
		return sseEmitter;
	}

	private void disconnect(Long groupId, Long userId, SseEmitter sseEmitter) {
		sseEmitterRepository.delete(groupId, userId, sseEmitter);

		MemberStatusSseResponse memberStatusSseResponse = MemberStatusSseResponse.builder()
			.userId(userId)
			.status(ConnectionStatus.OFFLINE)
			.build();
		sendToGroup(groupId, STATUS_UPDATE_EVENT, memberStatusSseResponse);
	}

	private void sendGroupMemberInfoList(Long groupId, SseEmitter sseEmitter) {
		List<GroupMemberSseResponse> groupMemberInfoList = getGroupMemberInfoList(groupId);
		sendData(sseEmitter, MEMBER_DATA_EVENT, groupMemberInfoList);
	}

	private List<GroupMemberSseResponse> getGroupMemberInfoList(Long groupId) {
		List<GroupMemberDto> groupMembers = groupMemberRepository.findByGroupId(groupId);
		Map<Object, Object> connectedUsersData = sseEmitterRepository.getConnectedUsers(groupId);
		Set<Object> connectedUserIds = connectedUsersData.keySet();

		return groupMembers.stream()
			.map(groupMember -> GroupMemberSseResponse.builder()
				.id(groupMember.getId())
				.nickname(groupMember.getNickname())
				.image(groupMember.getImage())
				.isHost(groupMember.isHost())
				.status(
					connectedUserIds.contains(groupMember.getId().toString()) ?
						ConnectionStatus.valueOf((String)connectedUsersData.get(groupMember.getId().toString()))
						: ConnectionStatus.OFFLINE)
				.studyTime(groupMember.getStudyTime() == null ? 0 : groupMember.getStudyTime())
				.groupId(groupMember.getGroupId())
				.build())
			.toList();
	}

	@Override
	public void sendFocusRoomMemberCountToGroup(Long groupId, int memberCount) {
		sendToGroup(groupId, FOCUS_ROOM_MEMBER_COUNT_EVENT, memberCount);
	}

	public void sendFocusRoomMemberCount(Long groupId, SseEmitter sseEmitter) {
		int focusRoomMemberCount = sseEmitterRepository.getFocusRoomMemberCount(groupId);
		sendData(sseEmitter, FOCUS_ROOM_MEMBER_COUNT_EVENT, focusRoomMemberCount);
	}

	@Override
	public void sendMemberStatusToGroup(Long groupId, Long userId, ConnectionStatus status) {
		sseEmitterRepository.updateStatus(groupId, userId, status);
		MemberStatusSseResponse memberStatusSseResponse = MemberStatusSseResponse.builder()
			.userId(userId)
			.status(status)
			.build();
		sendToGroup(groupId, STATUS_UPDATE_EVENT, memberStatusSseResponse);
	}

	private void sendToGroup(Long groupId, String eventName, Object data) {
		Map<Long, SseEmitter> groupSseEmitters = sseEmitterRepository.findAllByGroupId(groupId);
		groupSseEmitters.forEach((userId, sseEmitter) -> {
			if (sseEmitter != null) {
				sendData(sseEmitter, eventName, data);
			}
		});
	}

	private void sendData(SseEmitter sseEmitter, String eventName, Object data) {
		try {
			sseEmitter.send(SseEmitter.event()
				.name(eventName)
				.data(data));
		} catch (IOException e) {
			sseEmitter.complete();
		}
	}
}
