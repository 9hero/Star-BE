package com.mercury.star_be.studygroup.dto.response;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
public class StudyGroupListResponse {
    private Long id;
    private String name;
    private String image;
    private String description;
    private int maxCapacity;
    private int memberCount;
    private Boolean isPublic;
    private Boolean hasPassword;
    private LocalDateTime createdAt;

}
