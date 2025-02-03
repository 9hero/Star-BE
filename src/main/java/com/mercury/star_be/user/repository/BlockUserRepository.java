package com.mercury.star_be.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.mercury.star_be.user.entity.BlockUser;

public interface BlockUserRepository extends JpaRepository<BlockUser, Long> {

	boolean existsByUserIdAndBlockUserId(Long userId, Long blockUserId);
}
