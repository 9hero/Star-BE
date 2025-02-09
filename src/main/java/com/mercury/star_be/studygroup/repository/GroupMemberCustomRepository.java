package com.mercury.star_be.studygroup.repository;

import java.util.List;

import com.mercury.star_be.studygroup.dto.GroupMemberDto;

public interface GroupMemberCustomRepository {

	List<GroupMemberDto> findByGroupId(Long groupId);
}
