package com.mercury.star_be.studygroup.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class PaginationResponse<StudyGroupListResponse> {
    private List<StudyGroupListResponse> content;
    private int currentPage;      // 현재 페이지
    private boolean isLast;       // 마지막 페이지 여부


}