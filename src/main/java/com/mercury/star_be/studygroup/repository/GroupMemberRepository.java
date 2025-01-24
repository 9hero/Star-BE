package com.mercury.star_be.studygroup.repository;

import java.util.List;

import com.mercury.star_be.studygroup.entity.StudyGroup;
import com.mercury.star_be.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import com.mercury.star_be.studygroup.entity.GroupMember;

public interface GroupMemberRepository extends JpaRepository<GroupMember, Long> {

	List<GroupMember> findByGroupIdOrderByNicknameAsc(Long groupId);
	boolean existsByGroupIdAndMemberId(Long groupId, Long memberId);
}
