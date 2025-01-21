package com.mercury.star_be.studygroup.service;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mercury.star_be.global.error.BusinessException;
import com.mercury.star_be.global.error.code.StudyGroupErrorCode;
import com.mercury.star_be.studygroup.dto.request.StudyGroupCreateRequest;
import com.mercury.star_be.studygroup.dto.request.StudyGroupUpdateRequest;
import com.mercury.star_be.studygroup.dto.response.StudyGroupCreateResponse;
import com.mercury.star_be.studygroup.dto.response.StudyGroupDetailResponse;
import com.mercury.star_be.studygroup.dto.response.StudyGroupUpdateResponse;
import com.mercury.star_be.studygroup.entity.StudyGroup;
import com.mercury.star_be.studygroup.repository.StudyGroupRepository;

import lombok.RequiredArgsConstructor;

@Transactional(readOnly = true)
@Service
@RequiredArgsConstructor
public class StudyGroupServiceImpl implements StudyGroupService {

	private final StudyGroupRepository studyGroupRepository;

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

	public StudyGroup findById(Long id) {
		return studyGroupRepository.findById(id)
			.orElseThrow(() -> new BusinessException(StudyGroupErrorCode.STUDY_GROUP_NOT_FOUND));
	}
}
