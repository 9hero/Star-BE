package com.mercury.star_be.studygroup.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.mercury.star_be.studygroup.entity.GroupMember;

public interface GroupMemberRepository extends JpaRepository<GroupMember, Long> {

	List<GroupMember> findByGroupIdOrderByNicknameAsc(Long groupId);
}
