package com.mercury.star_be.studygroup.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.mercury.star_be.studygroup.entity.GroupMember;

public interface GroupMemberRepository extends JpaRepository<GroupMember, Long> {

	List<GroupMember> findByGroupIdOrderByNicknameAsc(Long groupId);
	boolean existsByGroupIdAndMemberId(Long groupId, Long memberId);
	void deleteByGroupIdAndMemberId(Long groupId, Long memberId);

	Optional<GroupMember> findFirstByGroupIdAndIdNotOrderByJoinedAtAsc(Long groupId, Long excludedMemberId);

	Optional<GroupMember> findByGroupIdAndMemberId(Long groupId, Long memberId);
}
