package com.mercury.star_be.studygroup.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import com.mercury.star_be.studygroup.dto.response.*;
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
		int size = 10;
		Pageable pageable = PageRequest.of(page, size);

		Page<StudyGroup> studyGroups = studyGroupRepository.findAllPublicByCreationDate(keyword, sort, direction, pageable);

		List<StudyGroupListResponse> content = studyGroups.getContent().stream()
				.map(studyGroup -> new StudyGroupListResponse(
						studyGroup.getId(),
						studyGroup.getName(),
						studyGroup.getDescription(),
						studyGroup.getImage(),
						studyGroup.getMaxCapacity(),
						studyGroup.getMemberCount(),
						studyGroup.isPublic(),
						studyGroup.hasPassword(),
						studyGroup.getPassword(),
						studyGroup.getCreatedAt()
				))
				.toList();

		return new PaginationResponse<>(
				content,
				studyGroups.getNumber(),
				studyGroups.isLast()
		);
	}

	public StudyGroup findById(Long id) {
		return studyGroupRepository.findById(id)
			.orElseThrow(() -> new BusinessException(StudyGroupErrorCode.STUDY_GROUP_NOT_FOUND));
	}
}
