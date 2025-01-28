package com.mercury.star_be.user.repository;

import com.mercury.star_be.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
}