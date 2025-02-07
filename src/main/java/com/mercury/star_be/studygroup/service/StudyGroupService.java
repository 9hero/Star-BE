package com.mercury.star_be.studygroup.service;

import java.util.List;

import com.mercury.star_be.global.error.BusinessException;
import com.mercury.star_be.studygroup.dto.request.ChangeGroupNicknameRequest;
import com.mercury.star_be.studygroup.dto.request.StudyGroupCreateRequest;
import com.mercury.star_be.studygroup.dto.request.StudyGroupUpdateRequest;
import com.mercury.star_be.studygroup.dto.response.ChangeGroupNicknameResponse;
import com.mercury.star_be.studygroup.dto.response.MyStudyGroupListResponse;
import com.mercury.star_be.studygroup.dto.response.PaginationResponse;
import com.mercury.star_be.studygroup.dto.response.StudyGroupCreateResponse;
import com.mercury.star_be.studygroup.dto.response.StudyGroupDetailResponse;
import com.mercury.star_be.studygroup.dto.response.StudyGroupEnterResponse;
import com.mercury.star_be.studygroup.dto.response.StudyGroupListResponse;
import com.mercury.star_be.studygroup.dto.response.StudyGroupUpdateResponse;

public interface StudyGroupService {

	StudyGroupCreateResponse createStudyGroup(StudyGroupCreateRequest studyGroupCreateRequest, String token);

	StudyGroupUpdateResponse updateStudyGroup(StudyGroupUpdateRequest studyGroupUpdateRequest, Long groupId,
		String token);

	StudyGroupDetailResponse getStudyGroup(Long groupId);

	StudyGroupEnterResponse enterStudyGroup(Long groupId, String token);

	PaginationResponse<StudyGroupListResponse> getStudyGroupList(String keyword, String sort, String direction,
		int page);

	void joinStudyGroup(Long groupId, String token, String password) throws BusinessException;

	void exitStudyGroup(Long groupId, String token) throws BusinessException;

	void changeHost(Long groupId, Long userId, Long newHostId);

	ChangeGroupNicknameResponse changeGroupNickname(Long userId, Long groupId,
		ChangeGroupNicknameRequest changeGroupNicknameRequest);

	List<MyStudyGroupListResponse> getMyStudyGroupList(String token);
}
