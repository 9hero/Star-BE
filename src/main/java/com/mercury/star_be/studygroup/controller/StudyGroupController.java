package com.mercury.star_be.studygroup.controller;

import com.mercury.star_be.studygroup.dto.response.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import com.mercury.star_be.global.common.ApiResponse;
import com.mercury.star_be.studygroup.dto.request.StudyGroupCreateRequest;
import com.mercury.star_be.studygroup.dto.request.StudyGroupUpdateRequest;
import com.mercury.star_be.studygroup.service.StudyGroupService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class StudyGroupController {

    private final StudyGroupService studyGroupService;

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

    @GetMapping("/api/groups")
    public ApiResponse<PaginationResponse<StudyGroupListResponse>> getStudyGroupList(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String sort,
            @RequestParam(required = false) String direction,
            @RequestParam(defaultValue = "0") int page) {

        PaginationResponse<StudyGroupListResponse> response =
                studyGroupService.getStudyGroupList(keyword, sort, direction, page);

        return ApiResponse.success(response);
    }
}
