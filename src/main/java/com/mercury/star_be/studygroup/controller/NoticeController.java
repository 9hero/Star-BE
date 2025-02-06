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

    //TODO: testcode 작성해야함
    @PostMapping("/api/groups/{groupId}/notices")
    public ApiResponse<NoticeCreateResponse> createNotice(
            @RequestBody @Valid NoticeCreateRequest request,
            @PathVariable(value = "groupId") Long groupId,
            @RequestHeader (value = "Authorization", required = false) String authorizationHeader)
    {
        // 토큰 처리
        String token = null;
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            token = authorizationHeader.substring(7);
        }
        NoticeCreateResponse noticeCreateResponse = noticeService.createNotice(request, groupId, token);
        return ApiResponse.success(noticeCreateResponse);
    }

    //TODO:testcode 작성해야함
    @PutMapping("/api/groups/{groupId}/notices/{noticeId}")
    public ApiResponse<NoticeUpdateResponse> updateNotice(
            @RequestBody @Valid NoticeUpdateRequest request,
            @PathVariable(value = "groupId") Long groupId,
            @RequestHeader (value = "Authorization", required = false) String authorizationHeader,
            @PathVariable(value = "noticeId") Long noticeId) {
        // 토큰 처리
        String token = null;
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            token = authorizationHeader.substring(7);
        }
        NoticeUpdateResponse noticeUpdateResponse = noticeService.updateNotice(request,groupId,token,noticeId);
        return ApiResponse.success(noticeUpdateResponse);
    }
    //TODO: testcode 작성해야함
    @DeleteMapping("/api/groups/{groupId}/notices/{noticeId}")
    public ApiResponse deleteNotice(
            @PathVariable(value = "groupId") Long groupId,
            @RequestHeader (value = "Authorization", required = false) String authorizationHeader,
            @PathVariable(value = "noticeId") Long noticeId) {
        // 토큰 처리
        String token = null;
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            token = authorizationHeader.substring(7);
        }
        noticeService.deleteNotice(groupId, token, noticeId);
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
