package com.mercury.star_be.studygroup.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mercury.star_be.global.error.BusinessException;
import com.mercury.star_be.global.error.code.StudyGroupErrorCode;
import com.mercury.star_be.studygroup.dto.request.StudyGroupCreateRequest;
import com.mercury.star_be.studygroup.dto.request.StudyGroupUpdateRequest;
import com.mercury.star_be.studygroup.dto.response.PaginationResponse;
import com.mercury.star_be.studygroup.dto.response.StudyGroupCreateResponse;
import com.mercury.star_be.studygroup.dto.response.StudyGroupDetailResponse;
import com.mercury.star_be.studygroup.dto.response.StudyGroupListResponse;
import com.mercury.star_be.studygroup.dto.response.StudyGroupUpdateResponse;
import com.mercury.star_be.studygroup.entity.GroupMember;
import com.mercury.star_be.studygroup.entity.StudyGroup;
import com.mercury.star_be.studygroup.repository.GroupMemberRepository;
import com.mercury.star_be.studygroup.repository.StudyGroupRepository;
import com.mercury.star_be.user.entity.User;
import com.mercury.star_be.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Transactional(readOnly = true)
@Service
@RequiredArgsConstructor
public class StudyGroupServiceImpl implements StudyGroupService {

	private final StudyGroupRepository studyGroupRepository;
	private final GroupMemberRepository groupMemberRepository;
	private final UserRepository userRepository;

	@Override
	@Transactional
	public StudyGroupCreateResponse createStudyGroup(StudyGroupCreateRequest studyGroupCreateRequest) {
		StudyGroup studyGroup = StudyGroup.builder()
			.name(studyGroupCreateRequest.getName())
			.description(studyGroupCreateRequest.getDescription())
			.image(studyGroupCreateRequest.getImage())
			.maxCapacity(studyGroupCreateRequest.getMaxCapacity())
			.memberCount(1)
			.hasPassword(studyGroupCreateRequest.hasPassword())
			.password(studyGroupCreateRequest.getPassword())
			.isPublic(studyGroupCreateRequest.getIsPublic())
			.createdAt(LocalDateTime.now())
			.build();

		// TODO: GroupMember에 사용자 추가 필요
		StudyGroup savedGroup = studyGroupRepository.save(studyGroup);
		return new StudyGroupCreateResponse(savedGroup.getId());
	}

	@Override
	@Transactional
	public StudyGroupUpdateResponse updateStudyGroup(StudyGroupUpdateRequest studyGroupUpdateRequest, Long groupId) {
		StudyGroup studyGroup = findById(groupId);
		int updatedMaxCapacity = studyGroupUpdateRequest.getMaxCapacity();
		if (studyGroup.getMemberCount() > updatedMaxCapacity) {
			throw new BusinessException(StudyGroupErrorCode.INVALID_MAX_CAPACITY);
		}

		studyGroup.updateStudyGroup(studyGroupUpdateRequest.getName(),
			studyGroupUpdateRequest.getDescription(),
			studyGroupUpdateRequest.getImage(),
			studyGroupUpdateRequest.getMaxCapacity(),
			studyGroupUpdateRequest.isPublic(),
			studyGroupUpdateRequest.hasPassword(),
			studyGroupUpdateRequest.getPassword());

		return StudyGroupUpdateResponse.builder()
			.id(studyGroup.getId())
			.name(studyGroup.getName())
			.description(studyGroup.getDescription())
			.image(studyGroup.getImage())
			.maxCapacity(studyGroup.getMaxCapacity())
			.memberCount(studyGroup.getMemberCount())
			.isPublic(studyGroup.isPublic())
			.hasPassword(studyGroup.hasPassword())
			.password(studyGroup.getPassword())
			.build();
	}

	@Override
	public StudyGroupDetailResponse getStudyGroup(Long groupId) {
		StudyGroup studyGroup = findById(groupId);
		return StudyGroupDetailResponse.builder()
			.id(studyGroup.getId())
			.name(studyGroup.getName())
			.description(studyGroup.getDescription())
			.image(studyGroup.getImage())
			.maxCapacity(studyGroup.getMaxCapacity())
			.memberCount(studyGroup.getMemberCount())
			.isPublic(studyGroup.isPublic())
			.hasPassword(studyGroup.hasPassword())
			.createdAt(studyGroup.getCreatedAt().toLocalDate())
			.build();
	}

	@Override
	public PaginationResponse<StudyGroupListResponse> getStudyGroupList(String keyword, String sort, String direction, int page) {
		int size = 20;
		Pageable pageable = PageRequest.of(page, size);

		Page<StudyGroup> studyGroups = studyGroupRepository.findAllPublicByCreationDate(keyword, sort, direction, pageable);

		List<StudyGroupListResponse> content = studyGroups.getContent().stream()
				.map(studyGroup -> StudyGroupListResponse.builder()
						.id(studyGroup.getId())
						.name(studyGroup.getName())
						.description(studyGroup.getDescription())
						.image(studyGroup.getImage())
						.maxCapacity(studyGroup.getMaxCapacity())
						.memberCount(studyGroup.getMemberCount())
						.isPublic(studyGroup.isPublic())
						.hasPassword(studyGroup.hasPassword())
						.createdAt(studyGroup.getCreatedAt())
						.build()
				)
				.toList();

		return new PaginationResponse<>(
				content,
				studyGroups.getNumber(),
				studyGroups.isLast()
		);
	}

	@Override
	@Transactional
	public void joinStudyGroup(Long groupId, Long userId) throws BusinessException {

		User user = userRepository.findById(userId).orElseThrow();

		// 가입하려는 그룹이 존재하지 않을때
		StudyGroup studyGroup = studyGroupRepository.findById(groupId)
				.orElseThrow(() -> new BusinessException(StudyGroupErrorCode.STUDY_GROUP_NOT_FOUND));

		// 가입하려는 그룹이 다 찼을떄
		if (studyGroup.getMemberCount() >= studyGroup.getMaxCapacity()) {
			throw new BusinessException(StudyGroupErrorCode.STUDY_GROUP_IS_FULL);
		}

		// 가입하려는 그룹에 이미 유저가 가입한 상태일때
		boolean isAlreadyJoined = groupMemberRepository.existsByGroupIdAndMemberId(studyGroup.getId(), user.getId());
		if (isAlreadyJoined) {
			throw new BusinessException(StudyGroupErrorCode.USER_ALREADY_EXIST_IN_GROUP);
		}

		// 그룹에 유져 추가
		GroupMember groupMember = GroupMember.builder()
				.group(studyGroup)
				.member(user)
				.isHost(false)
				.image(user.getImage())
				.nickname(user.getNickname())
				.joinedAt(LocalDateTime.now())
				.build();

		studyGroup.addMember(groupMember);
	}

	@Override
	@Transactional
	public void exitStudyGroup(Long groupId, Long userId) throws BusinessException {
		User user = userRepository.findById(userId)
				.orElseThrow();
		// 그룹이 존재하는지
		StudyGroup studyGroup = studyGroupRepository.findById(groupId)
				.orElseThrow(() -> new BusinessException(StudyGroupErrorCode.STUDY_GROUP_NOT_FOUND));
		// 탈퇴하려는 사람이 그룹에 존재하는지
		GroupMember groupMember = groupMemberRepository.findByGroupIdAndMemberId(groupId,userId)
				.orElseThrow(() -> new BusinessException(StudyGroupErrorCode.USER_NOT_EXIST_IN_GROUP));
		// 그룹의 멤버가 1명만 남아 있는 경우 (호스트 == 마지막 유저)
		if (studyGroup.getMemberCount() == 1) {
			// 그룹 삭제
			groupMemberRepository.deleteByGroupIdAndMemberId(groupId, userId);
			studyGroupRepository.delete(studyGroup);
			return; // 여기서 종료
		}
		// 유저가 호스트인 경우 새 호스트 지정
		if(groupMember.isHost()) {
			GroupMember newHost = groupMemberRepository.findFirstByGroupIdAndIdNotOrderByJoinedAtAsc(studyGroup.getId(), groupMember.getId())
					.orElseThrow(() -> new BusinessException(StudyGroupErrorCode.STUDY_GROUP_IS_EMPTY));
			newHost.updateGroupMember(
					newHost.getId(),
					newHost.getNickname(),
					newHost.getImage(),
					true, // 새 호스트 설정
					newHost.getGroup(),
					newHost.getMember(),
					newHost.getJoinedAt()
			);
			// TODO: @Transactional 과 관련된 질문
			groupMemberRepository.save(newHost);
		}
		// 그룹 멤버 관계 삭제
		groupMemberRepository.deleteByGroupIdAndMemberId(studyGroup.getId(), user.getId());
		// 그룹의 멤버 카운트 감소
		studyGroup.decrementMemberCount();

	}

	//TODO: token 받아서 처리하기, transactional?
	@Override
	@Transactional
	public void changeHost(Long groupId, Long userId, Long newHostId) {

		GroupMember currentHost = groupMemberRepository.findByGroupIdAndMemberId(groupId, userId)
				.orElseThrow(() -> new BusinessException(StudyGroupErrorCode.USER_NOT_EXIST_IN_GROUP));
		GroupMember newHost = groupMemberRepository.findByGroupIdAndMemberId(groupId, newHostId)
				.orElseThrow(() -> new BusinessException(StudyGroupErrorCode.USER_NOT_EXIST_IN_GROUP));

		currentHost.updateGroupMember(
				currentHost.getId(),
				currentHost.getNickname(),
				currentHost.getImage(),
				false, // 호트트 권한 박탈
				currentHost.getGroup(),
				currentHost.getMember(),
				currentHost.getJoinedAt()
		);
		newHost.updateGroupMember(
				newHost.getId(),
				newHost.getNickname(),
				newHost.getImage(),
				true, // 새 호스트 설정
				newHost.getGroup(),
				newHost.getMember(),
				newHost.getJoinedAt()
		);
		// 변경된 엔티티 저장
//		groupMemberRepository.save(currentHost);
//		groupMemberRepository.save(newHost);
	}

	public StudyGroup findById(Long id) {
		return studyGroupRepository.findById(id)
			.orElseThrow(() -> new BusinessException(StudyGroupErrorCode.STUDY_GROUP_NOT_FOUND));
	}
}
