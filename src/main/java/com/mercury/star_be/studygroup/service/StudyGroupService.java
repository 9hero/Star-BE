package com.mercury.star_be.studygroup.service;

import com.mercury.star_be.studygroup.dto.request.StudyGroupCreateRequest;
import com.mercury.star_be.studygroup.dto.request.StudyGroupUpdateRequest;
import com.mercury.star_be.studygroup.dto.response.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;


public interface StudyGroupService {

	StudyGroupCreateResponse createStudyGroup(StudyGroupCreateRequest studyGroupCreateRequest);

	StudyGroupUpdateResponse updateStudyGroup(StudyGroupUpdateRequest studyGroupUpdateRequest, Long groupId);

	StudyGroupDetailResponse getStudyGroup(Long groupId);

	PaginationResponse<StudyGroupListResponse> getStudyGroupList(String keyword, String sort, String direction, int page);
}
