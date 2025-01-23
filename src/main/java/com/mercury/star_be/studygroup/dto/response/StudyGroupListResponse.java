package com.mercury.star_be.studygroup.dto.response;

import jakarta.persistence.Column;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

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
    private String password;
    private LocalDateTime createdAt;

}
