package com.mercury.star_be.studygroup.repository;

import com.mercury.star_be.studygroup.entity.Notice;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NoticeRepository extends JpaRepository<Notice, Long> {
}
