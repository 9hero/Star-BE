package com.mercury.star_be.studygroup.controller;


import java.util.List;

import com.mercury.star_be.global.common.ApiResponse;
import com.mercury.star_be.studygroup.dto.request.NoticeCreateRequest;
import com.mercury.star_be.studygroup.dto.request.NoticeUpdateRequest;
import com.mercury.star_be.studygroup.dto.response.NoticeCreateResponse;
import com.mercury.star_be.studygroup.dto.response.NoticeResponse;
import com.mercury.star_be.studygroup.dto.response.NoticeUpdateResponse;
import com.mercury.star_be.studygroup.service.NoticeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class NoticeController {

    private final NoticeService noticeService;

    //TODO: user id 토큰으로 변경 testcode 작성해야함
    @PostMapping("/api/groups/{groupId}/notices/{userId}")
    public ApiResponse<NoticeCreateResponse> createNotice(
            @RequestBody @Valid NoticeCreateRequest request,
            @PathVariable(value = "groupId") Long groupId,
            @PathVariable(value = "userId") Long userId)
    {
        NoticeCreateResponse noticeCreateResponse = noticeService.createNotice(request, groupId, userId);
        return ApiResponse.success(noticeCreateResponse);
    }
    //TODO: user id 토큰으로 변경 testcode 작성해야함
    @PutMapping("/api/groups/{groupId}/notices/{userId}/{noticeId}")
    public ApiResponse<NoticeUpdateResponse> updateNotice(
            @RequestBody @Valid NoticeUpdateRequest request,
            @PathVariable(value = "groupId") Long groupId,
            @PathVariable(value = "userId") Long userId,
            @PathVariable(value = "noticeId") Long noticeId) {
        NoticeUpdateResponse noticeUpdateResponse = noticeService.updateNotice(request,groupId,userId,noticeId);
        return ApiResponse.success(noticeUpdateResponse);
    }
    //TODO: user id 토큰으로 변경 testcode 작성해야함
    @DeleteMapping("/api/groups/{groupId}/notices/{userId}/{noticeId}")
    public ApiResponse deleteNotice(
            @PathVariable(value = "groupId") Long groupId,
            @PathVariable(value = "userId") Long userId,
            @PathVariable(value = "noticeId") Long noticeId) {

        noticeService.deleteNotice(groupId, userId, noticeId);
        return ApiResponse.success();
    }

    @GetMapping("/api/groups/{groupId}/notices")
    public ApiResponse<List<NoticeResponse>> getNoticeList(@PathVariable(value = "groupId") Long groupId) {
        List<NoticeResponse> noticeList = noticeService.getNoticeList(groupId);
        return ApiResponse.success(noticeList);
    }

    @GetMapping("/api/groups/{groupId}/notices/{noticeId}")
    public ApiResponse<NoticeResponse> getNotice(
        @PathVariable(value = "groupId") Long groupId,
        @PathVariable(value = "noticeId") Long noticeId) {
        NoticeResponse noticeResponse = noticeService.getNotice(groupId, noticeId);
        return ApiResponse.success(noticeResponse);
    }
}
