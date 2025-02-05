package com.mercury.star_be.studygroup.controller;

import com.mercury.star_be.studygroup.dto.request.ChangeGroupNicknameRequest;
import com.mercury.star_be.studygroup.dto.request.StudyGroupJoinRequest;
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

@RestController
@RequiredArgsConstructor
public class StudyGroupController {

    private final StudyGroupService studyGroupService;

    @PostMapping("/api/groups")
    public ApiResponse<StudyGroupCreateResponse> createStudyGroup(
            @RequestBody @Valid StudyGroupCreateRequest studyGroupCreateRequest,
            @RequestHeader (value = "Authorization", required = false) String authorizationHeader
    ) {
        // 토큰 처리
        String token = null;
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            token = authorizationHeader.substring(7);
        }

        StudyGroupCreateResponse studyGroupCreateResponse = studyGroupService.createStudyGroup(studyGroupCreateRequest, token);
        return ApiResponse.success(studyGroupCreateResponse);
    }

    @PutMapping("/api/groups/{groupId}")
    public ApiResponse<StudyGroupUpdateResponse> updateStudyGroup(
            @RequestBody @Valid StudyGroupUpdateRequest studyGroupUpdateRequest,
            @PathVariable(value = "groupId") Long groupId,
            @RequestHeader (value = "Authorization", required = false) String authorizationHeader
    ) {// 토큰 처리
        String token = null;
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            token = authorizationHeader.substring(7);
        }
        StudyGroupUpdateResponse studyGroupUpdateResponse = studyGroupService.updateStudyGroup(studyGroupUpdateRequest,
            groupId, token);
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

    @PostMapping("/api/groups/{groupId}/join")
    public ApiResponse joinStudyGroup(
            @PathVariable(value = "groupId") Long groupId,
            @RequestHeader (value = "Authorization", required = false) String authorizationHeader,
            @RequestBody (required = false)StudyGroupJoinRequest studyGroupJoinRequest
            ) {
        // 토큰 처리
        String token = null;
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            token = authorizationHeader.substring(7);
        }
        String password = studyGroupJoinRequest != null ? studyGroupJoinRequest.getPassword() : null;
        studyGroupService.joinStudyGroup(groupId, token, password);
        return ApiResponse.success();
    }

    //TODO: testcode 미작성 추후에 token 받아서 처리해야함
    @DeleteMapping("/api/groups/{groupId}/exit")
    public ApiResponse exitStudyGroup(
            @PathVariable(value = "groupId") Long groupId,
            @RequestHeader (value = "Authorization", required = false) String authorizationHeader
    ) {
        // 토큰 처리
        String token = null;
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            token = authorizationHeader.substring(7);
        }
        studyGroupService.exitStudyGroup(groupId, token);
        return ApiResponse.success();
    }

    //TODO: testcode 미작성 추후에 token 받아서 처리해야함
    @PutMapping("/api/groups/{groupId}/change-admin/{oldHostId}/{newHostId}")
    public ApiResponse changeGroupHost(
            @PathVariable(value = "groupId") Long groupId,
            @PathVariable(value = "oldHostId") Long loggedInUserId, // replace by token later
            @PathVariable(value = "newHostId") Long newHostId
    ) {
        studyGroupService.changeHost(groupId, loggedInUserId, newHostId);
        return ApiResponse.success();
    }

    @PatchMapping("/api/users/{userId}/groups/{groupId}/change-nickname")
    public ApiResponse<ChangeGroupNicknameResponse> changeGroupNickname(
        @RequestBody ChangeGroupNicknameRequest changeGroupNicknameRequest,
        @PathVariable(value = "userId") Long userId,
        @PathVariable(value = "groupId") Long groupId) {
        ChangeGroupNicknameResponse changeGroupNicknameResponse = studyGroupService.changeGroupNickname(userId, groupId,
            changeGroupNicknameRequest);
        return ApiResponse.success(changeGroupNicknameResponse);
    }
}
