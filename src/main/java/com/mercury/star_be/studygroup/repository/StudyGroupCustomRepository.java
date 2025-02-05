package com.mercury.star_be.studygroup.repository;

import com.mercury.star_be.studygroup.dto.response.MyStudyGroupListResponse;
import com.mercury.star_be.studygroup.entity.StudyGroup;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface StudyGroupCustomRepository {
    Page<StudyGroup> findAllPublicByCreationDate(String keyword, String sort, String direction, Pageable pageable);
    List<MyStudyGroupListResponse> findMyStudyGroupList(Long memberId);
}
