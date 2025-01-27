package com.mercury.star_be.studygroup.repository;

import java.util.List;

import com.mercury.star_be.studygroup.dto.response.NoticeResponse;

public interface NoticeCustomRepository {

	List<NoticeResponse> findAllByGroupId(Long groupId);
}
