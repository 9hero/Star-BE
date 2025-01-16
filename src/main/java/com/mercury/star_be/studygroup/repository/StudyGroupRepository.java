package com.mercury.star_be.studygroup.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.mercury.star_be.studygroup.entity.StudyGroup;

public interface StudyGroupRepository extends JpaRepository<StudyGroup, Long> {
}
