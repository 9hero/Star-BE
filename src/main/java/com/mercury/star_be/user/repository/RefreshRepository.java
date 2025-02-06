package com.mercury.star_be.user.repository;

import com.mercury.star_be.user.entity.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RefreshRepository extends JpaRepository<RefreshToken, Long> {

    Optional<RefreshToken> findByUser_Id(Long userId);

}
