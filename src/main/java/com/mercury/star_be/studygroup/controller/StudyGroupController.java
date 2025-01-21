package com.mercury.star_be.studygroup.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.mercury.star_be.global.common.ApiResponse;
import com.mercury.star_be.studygroup.dto.request.StudyGroupCreateRequest;
import com.mercury.star_be.studygroup.dto.request.StudyGroupUpdateRequest;
import com.mercury.star_be.studygroup.dto.response.StudyGroupCreateResponse;
import com.mercury.star_be.studygroup.dto.response.StudyGroupDetailResponse;
import com.mercury.star_be.studygroup.dto.response.StudyGroupUpdateResponse;
import com.mercury.star_be.studygroup.service.StudyGroupService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class StudyGroupController {

    private final StudyGroupService studyGroupService;

    @GetMapping("/api/testData")
    public ResponseEntity<String> testData() {
        return ResponseEntity.ok("Hello front!! im backend Data~");
    }

    @PostMapping("/api/groups")
    public ApiResponse<StudyGroupCreateResponse> createStudyGroup(@RequestBody @Valid StudyGroupCreateRequest studyGroupCreateRequest) {
        StudyGroupCreateResponse studyGroupCreateResponse = studyGroupService.createStudyGroup(studyGroupCreateRequest);
        return ApiResponse.success(studyGroupCreateResponse);
    }

    @PutMapping("/api/groups/{groupId}")
    public ApiResponse<StudyGroupUpdateResponse> updateStudyGroup(@RequestBody @Valid StudyGroupUpdateRequest studyGroupUpdateRequest,
        @PathVariable(value = "groupId") Long groupId) {
        StudyGroupUpdateResponse studyGroupUpdateResponse = studyGroupService.updateStudyGroup(studyGroupUpdateRequest,
            groupId);
        return ApiResponse.success(studyGroupUpdateResponse);
    }

    @GetMapping("/api/groups/{groupId}")
    public ApiResponse<StudyGroupDetailResponse> getStudyGroup(@PathVariable(value = "groupId") Long groupId) {
        StudyGroupDetailResponse studyGroupDetailResponse = studyGroupService.getStudyGroup(groupId);
        return ApiResponse.success(studyGroupDetailResponse);
    }
}
