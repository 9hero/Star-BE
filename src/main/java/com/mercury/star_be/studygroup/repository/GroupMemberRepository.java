package com.mercury.star_be.studygroup.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.mercury.star_be.studygroup.entity.GroupMember;
import org.springframework.data.jpa.repository.Query;

public interface GroupMemberRepository extends JpaRepository<GroupMember, Long> {

	List<GroupMember> findByGroupIdOrderByNicknameAsc(Long groupId);
	boolean existsByGroupIdAndMemberId(Long groupId, Long memberId);
	void deleteByGroupIdAndMemberId(Long groupId, Long memberId);

	Optional<GroupMember> findFirstByGroupIdAndIdNotOrderByJoinedAtAsc(Long groupId, Long excludedMemberId);

	Optional<GroupMember> findByGroupIdAndMemberId(Long groupId, Long memberId);


	// JPQL을 통한 데이터 조회
	@Query("SELECT gm.group.id FROM GroupMember gm WHERE gm.member.id = :memberId")
	List<Long> findGroupIdsByMemberId(Long memberId);
	List<GroupMember> findByGroupIdOrderByJoinedAtAsc(Long groupId);
}
