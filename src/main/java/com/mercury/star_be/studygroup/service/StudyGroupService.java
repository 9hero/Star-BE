package com.mercury.star_be.studygroup.service;

import com.mercury.star_be.global.error.BusinessException;
import com.mercury.star_be.studygroup.dto.request.ChangeGroupNicknameRequest;
import com.mercury.star_be.studygroup.dto.request.StudyGroupCreateRequest;
import com.mercury.star_be.studygroup.dto.request.StudyGroupUpdateRequest;
import com.mercury.star_be.studygroup.dto.response.*;


public interface StudyGroupService {

	StudyGroupCreateResponse createStudyGroup(StudyGroupCreateRequest studyGroupCreateRequest, String token);

	StudyGroupUpdateResponse updateStudyGroup(StudyGroupUpdateRequest studyGroupUpdateRequest, Long groupId);

	StudyGroupDetailResponse getStudyGroup(Long groupId);

	PaginationResponse<StudyGroupListResponse> getStudyGroupList(String keyword, String sort, String direction, int page);

	void joinStudyGroup(Long groupId, String token, String password) throws BusinessException;

	void exitStudyGroup(Long groupId, Long userId) throws BusinessException;

	void changeHost (Long groupId, Long userId, Long newHostId);

	ChangeGroupNicknameResponse changeGroupNickname(Long userId, Long groupId, ChangeGroupNicknameRequest changeGroupNicknameRequest);
}
