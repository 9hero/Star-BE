package com.mercury.star_be.studygroup.service;

import com.mercury.star_be.studygroup.dto.request.StudyGroupCreateRequest;
import com.mercury.star_be.studygroup.dto.response.StudyGroupCreateResponse;

public interface StudyGroupService {

	StudyGroupCreateResponse createStudyGroup(StudyGroupCreateRequest studyGroupCreateRequest);
}
