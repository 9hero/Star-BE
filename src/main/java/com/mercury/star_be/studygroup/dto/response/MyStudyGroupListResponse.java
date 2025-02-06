package com.mercury.star_be.studygroup.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
public class MyStudyGroupListResponse {
    private Long id;
    private String imageUrl;
    private String name;
}
