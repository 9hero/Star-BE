package com.mercury.star_be.studygroup.service;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mercury.star_be.studygroup.dto.request.StudyGroupCreateRequest;
import com.mercury.star_be.studygroup.dto.response.StudyGroupCreateResponse;
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
			.isPublic(studyGroupCreateRequest.isPublic())
			.createdAt(LocalDateTime.now())
			.build();

		// TODO: GroupMember에 사용자 추가 필요
		StudyGroup savedGroup = studyGroupRepository.save(studyGroup);
		return new StudyGroupCreateResponse(savedGroup.getId());
	}
}
