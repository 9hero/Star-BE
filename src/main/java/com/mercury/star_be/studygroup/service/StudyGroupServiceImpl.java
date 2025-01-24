package com.mercury.star_be.studygroup.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import com.mercury.star_be.studygroup.dto.response.*;
import com.mercury.star_be.studygroup.entity.GroupMember;
import com.mercury.star_be.studygroup.repository.GroupMemberRepository;
import com.mercury.star_be.user.entity.User;
import com.mercury.star_be.user.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mercury.star_be.global.error.BusinessException;
import com.mercury.star_be.global.error.code.StudyGroupErrorCode;
import com.mercury.star_be.studygroup.dto.request.StudyGroupCreateRequest;
import com.mercury.star_be.studygroup.dto.request.StudyGroupUpdateRequest;
import com.mercury.star_be.studygroup.entity.StudyGroup;
import com.mercury.star_be.studygroup.repository.StudyGroupRepository;

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
			.password(studyGroup.getPassword())
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
						.password(studyGroup.getPassword())
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
		//가입 전 전처리
		// 가입하려는 그룹이 다 찼을떄
		// 가입하려는 그룹이 존재하지 않을때
		// 가입하려는 그룹에 이미 유저가 가입한 상태일때
		User user = userRepository.findById(userId).orElseThrow();
		StudyGroup studyGroup = studyGroupRepository.findById(groupId)
				.orElseThrow(() -> new BusinessException(StudyGroupErrorCode.STUDY_GROUP_NOT_FOUND));
		if (studyGroup.getMemberCount() >= studyGroup.getMaxCapacity()) {
			throw new BusinessException(StudyGroupErrorCode.STUDY_GROUP_IS_FULL);
		}
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
				.build();

		studyGroup.addMember(groupMember);
	}

	public StudyGroup findById(Long id) {
		return studyGroupRepository.findById(id)
			.orElseThrow(() -> new BusinessException(StudyGroupErrorCode.STUDY_GROUP_NOT_FOUND));
	}
}
