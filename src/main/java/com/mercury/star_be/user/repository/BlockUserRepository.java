package com.mercury.star_be.user.repository;

import java.util.List;
import java.util.Optional;

import com.mercury.star_be.user.dto.response.BlockUserListResponse;
import org.springframework.data.jpa.repository.JpaRepository;

import com.mercury.star_be.user.entity.BlockUser;

public interface BlockUserRepository extends JpaRepository<BlockUser, Long>, BlockUserCustomRepository {

	boolean existsByUserIdAndBlockUserId(Long userId, Long blockUserId);

	Optional<BlockUser> findByUserIdAndBlockUserId(Long userId, Long blockUserId);
	List<BlockUser> findAllBlockUsersByUserId(Long userId);
}
