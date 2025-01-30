package com.mercury.star_be.studygroup.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.mercury.star_be.studygroup.entity.Notice;

public interface NoticeRepository extends JpaRepository<Notice, Long>, NoticeCustomRepository {
}
