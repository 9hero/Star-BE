package com.mercury.star_be.studygroup.controller;

import java.util.List;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.mercury.star_be.global.common.ApiResponse;
import com.mercury.star_be.studygroup.dto.request.ChangeGroupNicknameRequest;
import com.mercury.star_be.studygroup.dto.request.StudyGroupCreateRequest;
import com.mercury.star_be.studygroup.dto.request.StudyGroupJoinRequest;
import com.mercury.star_be.studygroup.dto.request.StudyGroupUpdateRequest;
import com.mercury.star_be.studygroup.dto.response.ChangeGroupNicknameResponse;
import com.mercury.star_be.studygroup.dto.response.MyStudyGroupListResponse;
import com.mercury.star_be.studygroup.dto.response.PaginationResponse;
import com.mercury.star_be.studygroup.dto.response.StudyGroupCreateResponse;
import com.mercury.star_be.studygroup.dto.response.StudyGroupDetailResponse;
import com.mercury.star_be.studygroup.dto.response.StudyGroupEnterResponse;
import com.mercury.star_be.studygroup.dto.response.StudyGroupListResponse;
import com.mercury.star_be.studygroup.dto.response.StudyGroupUpdateResponse;
import com.mercury.star_be.studygroup.service.StudyGroupService;
import com.mercury.star_be.user.util.JwtUtil;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class StudyGroupController {

	private final StudyGroupService studyGroupService;

	@PostMapping("/api/groups")
	public ApiResponse<StudyGroupCreateResponse> createStudyGroup(
		@RequestBody @Valid StudyGroupCreateRequest studyGroupCreateRequest,
		Authentication auth
	) {
		Long userId = JwtUtil.getAuthenticatedUser(auth).getId();
		StudyGroupCreateResponse studyGroupCreateResponse = studyGroupService.createStudyGroup(studyGroupCreateRequest,
			userId);
		return ApiResponse.success(studyGroupCreateResponse);
	}

	@PutMapping("/api/groups/{groupId}")
	public ApiResponse<StudyGroupUpdateResponse> updateStudyGroup(
		@RequestBody @Valid StudyGroupUpdateRequest studyGroupUpdateRequest,
		@PathVariable(value = "groupId") Long groupId,
		Authentication auth
	) {
		Long userId = JwtUtil.getAuthenticatedUser(auth).getId();
		StudyGroupUpdateResponse studyGroupUpdateResponse = studyGroupService.updateStudyGroup(studyGroupUpdateRequest,
			groupId, userId);
		return ApiResponse.success(studyGroupUpdateResponse);
	}

	@GetMapping("/api/groups/{groupId}")
	public ApiResponse<StudyGroupDetailResponse> getStudyGroup(@PathVariable(value = "groupId") Long groupId) {
		StudyGroupDetailResponse studyGroupDetailResponse = studyGroupService.getStudyGroup(groupId);
		return ApiResponse.success(studyGroupDetailResponse);
	}

	@GetMapping("/api/groups/{groupId}/enter")
	public ApiResponse<StudyGroupEnterResponse> enterStudyGroup(
		@PathVariable(value = "groupId") Long groupId,
		Authentication auth
	) {
		Long userId = JwtUtil.getAuthenticatedUser(auth).getId();
		StudyGroupEnterResponse studyGroupEnterResponse = studyGroupService.enterStudyGroup(groupId, userId);
		return ApiResponse.success(studyGroupEnterResponse);
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
		@RequestBody(required = false) StudyGroupJoinRequest studyGroupJoinRequest,
		Authentication auth
	) {
		Long userId = JwtUtil.getAuthenticatedUser(auth).getId();
		String password = studyGroupJoinRequest != null ? studyGroupJoinRequest.getPassword() : null;
		studyGroupService.joinStudyGroup(groupId, userId, password);
		return ApiResponse.success();
	}

	//TODO: testcode 미작성 추후에 token 받아서 처리해야함
	@DeleteMapping("/api/groups/{groupId}/exit")
	public ApiResponse exitStudyGroup(
		@PathVariable(value = "groupId") Long groupId,
		Authentication auth
	) {
		Long userId = JwtUtil.getAuthenticatedUser(auth).getId();
		studyGroupService.exitStudyGroup(groupId, userId);
		return ApiResponse.success();
	}

	//TODO: testcode 미작성
	@PutMapping("/api/groups/{groupId}/change-admin/{newHostId}")
	public ApiResponse changeGroupHost(
		@PathVariable(value = "groupId") Long groupId,
		@PathVariable(value = "newHostId") Long newHostId,
		Authentication auth
	) {
		Long loggedInUserId = JwtUtil.getAuthenticatedUser(auth).getId();
		studyGroupService.changeHost(groupId, loggedInUserId, newHostId);
		return ApiResponse.success();
	}

	@PatchMapping("/api/users/groups/{groupId}/change-nickname")
	public ApiResponse<ChangeGroupNicknameResponse> changeGroupNickname(
		@RequestBody ChangeGroupNicknameRequest changeGroupNicknameRequest,
		@PathVariable(value = "groupId") Long groupId,
		Authentication auth) {
		Long userId = JwtUtil.getAuthenticatedUser(auth).getId();
		ChangeGroupNicknameResponse changeGroupNicknameResponse = studyGroupService.changeGroupNickname(userId, groupId,
			changeGroupNicknameRequest);
		return ApiResponse.success(changeGroupNicknameResponse);
	}

	@GetMapping("/api/groups/myGroups")
	public ApiResponse<List<MyStudyGroupListResponse>> getMyStudyGroupList(Authentication auth) {
		Long userId = JwtUtil.getAuthenticatedUser(auth).getId();
		List<MyStudyGroupListResponse> myStudyGroupListResponse = studyGroupService.getMyStudyGroupList(userId);
		return ApiResponse.success(myStudyGroupListResponse);
	}

}
