package com.mercury.star_be.studygroup.service;

import java.util.List;

import com.mercury.star_be.studygroup.dto.request.NoticeCreateRequest;
import com.mercury.star_be.studygroup.dto.request.NoticeUpdateRequest;
import com.mercury.star_be.studygroup.dto.response.NoticeCreateResponse;
import com.mercury.star_be.studygroup.dto.response.NoticeResponse;
import com.mercury.star_be.studygroup.dto.response.NoticeUpdateResponse;

public interface NoticeService {

    NoticeCreateResponse createNotice(NoticeCreateRequest request, Long groupId, String token);
    NoticeUpdateResponse updateNotice(NoticeUpdateRequest request, Long groupId, String token, Long noticeId);
    void deleteNotice(Long groupId, String token, Long noticeId);
    List<NoticeResponse> getNoticeList(Long groupId);
    NoticeResponse getNotice(Long groupId, Long noticeId);
}
